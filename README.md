# Mesh API Authentication

The Mesh API uses [HMAC](https://en.wikipedia.org/wiki/HMAC) Authentication for authenticating requests.
Each request made to the API must carry a "signature", which is an output of the HMAC-SHA256 algorithm.
The signature created by concatenating selected HTTP headers and using a secret key.

When the API receives the request, it will fetch the secret and perform the same signature calculation. If the signature that
is provided in HTTP request matches, then the operation is allowed, otherwise the request will be rejected and 401 status code returned.

As an opposite to token based authentication, the signature is tied to the HTTP headers with unfixed values, thus makes impossible to reuse (reply) the signature for different requests.

## Examples

This repository contains an examples on how perform authentication to Mesh API in the following languages:

* [Postman](./postman)
* [Python 3+](./python3)
* [Node.js 10+](./nodejs)
* [Java 8+](./java)
* [.NET C#](./dotnet)

If your language of your choice is not listed above, please follow instructions below on how to construct this type of request.

## Making Request

In order to authenticate your request, the following HTTP request headers must be included:

* `Date` - time stamp of request in format of [ISO-8601](http://en.wikipedia.org/wiki/ISO_8601). Example: `2019-11-07T11:37:32.510Z`. See [details](#Timestamp).
* `x-mesh-nonce` - unique identifier of the request that is sent to the API. See [details](#Nonce).
* `Authorization` - composed of four components: an algorithm declaration (scheme), API KEY, list of header names that used in signature, and the calculated signature. All those components structured in format that described in the [next section](#constructing-authorization-header).

So the example of such request may look like this:

```bash
curl -X GET \
  https://{host}/status \
  -H 'Date: 2019-11-07T11:37:32.510Z' \
  -H 'x-mesh-nonce: 4c97634c' \
  -H 'Authorization: HMAC-SHA256 Credential=${api-key};SignedHeaders=Date,x-mesh-nonce;Signature=${signature}'
```

Now let's drill down how to construct `Authorization` header.

### Constructing authorization header

Like already shown above, the header has the following structure:
```
HMAC-SHA256 Credential=;SignedHeaders=;Signature=
```

It consist of 2 parts separated by whitespace:
* `HMAC-SHA256` - scheme, which indicates the type of authorization and which HASH algorithm used to generate signature. This is a constant value.
* `Credential=;SignedHeaders=;Signature=` - authorization parameters list, separated by semicolon. Parameters names are case insensitive and all 3 of them are required. 
  * `Credential` - your **API KEY**
  * `SignedHeaders` - a list of HTTP request headers names separated by comma, that used to construct the signature. Usually the value will be `Date,x-mesh-nonce`.
  * `Signature` - base64 encoded SHA256 hash of concatenated headers that appeared in `SignedHeaders` and in the same order. 

So, in order to generate the signature:
1. Concatenate headers, where each one separated by line terminator `\n` and each line has a **lower case** header name and value separated by colon: `{lower_case_header_name}:{header_value}`. Example: `date:2019-11-07T11:37:32.510Z\nx-mesh-nonce:4c97634c`
1. Generate HMAC-SHA256 with message being the payload from above and the key is the **API SECRET**
1. Encode the result of hash function to base64 string

### Timestamp

A valid timestamp is mandatory for authenticated requests and must be within 5 minutes of the API system time when the request is received. You will need to ensure that the clock of the system from which the request is sent is accurate.

The timestamp must be passed in `Date` field as described in [RFC](https://tools.ietf.org/html/rfc7231#section-7.1.1.2).

### Nonce

[Nonce](https://en.wikipedia.org/wiki/Cryptographic_nonce) identifies a unique request made to a specific API operation. One of the main purposes of the nonce is to prevent replay attacks and detect duplicate requests to API.

If you'll try to reuse the same nonce to the same API operation that was successfully completed before, the API will return 403.

