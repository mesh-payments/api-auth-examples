# Mesh API Authentication

The Mesh API uses [HMAC](https://en.wikipedia.org/wiki/HMAC) Authentication for authenticating requests.
Each request made to the API must carry a "signature", which is an output of the HMAC-SHA256 algorithm.
The signature created by concatenating selected HTTP headers and using a secret key.

When the API receives the request, it will fetch the secret and perform the same signature calculation. If the signature that
is provided in HTTP request matches, then the operation is allowed, otherwise the request will be rejected and 401 status code returned.

As an opposite to token based authentication, the signature is tied to the HTTP headers with unfixed values, thus makes impossible to reuse (reply) the signature for different requests.

## Making Request

In order to authenticate your request, the following HTTP request headers must be included:

* `Date` - timestamp of request in format of [ISO-8601](http://en.wikipedia.org/wiki/ISO_8601). Example: `2019-11-07T11:37:32.510Z`. See [details](###Timestamp).
* `x-mesh-nonce` - unique identifier of the request that is sent to the API. See [details](#Nonce).
* `Authorization` - composed of four components: an algorithm declaration (scheme), api key, list of header names that used in signature, and the calculated signature. All those components structured in format that described in the [next section](#constructing-authorization-header).

So the example of such request may look like this:

```bash
curl -X GET \
  https://{host}/status \
  -H 'Date: Wed, 06 Nov 2019 21:37:48 GMT' \
  -H 'x-mesh-nonce: 4c97634c-6abe-4ef6-a2f7-4891bdcbbfba' \
  -H 'Authorization: HMAC-SHA256 Credential=${api-key};SignedHeaders=Date,x-mesh-nonce;Signature=${generated-signature}'
```

Now let's drill down how to construct `Authorization` header.

### Constructing authorization header

has the following format `HMAC-SHA256 Credential={api-access-key};SignedHeaders=Date,x-mesh-nonce;Signature={base64-encoded-signature}`.
The signature itself contains all headers in the order they appear in `SignedHeaders`, each one separated by line terminator `\n` (without line terminator at the very end) and each line is: `{lower_case_header_name}:{header_value}`

### Timestamp

* A valid time stamp is mandatory for authenticated requests and must be within 5 minutes of the API system time when the request is received.
* replay attacks
* clock scew
### Nonce

* https://en.wikipedia.org/wiki/Cryptographic_nonce
* reply attack
* when to retry

## Examples

This repository contains an example in the following languages:

* [Postman](./postman)
* [Python 3+](./python3)
