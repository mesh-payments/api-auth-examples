<?php

function call_api(string $host, string $key, string $secret) 
{
    if(empty($host) || empty($key) || empty($secret)) {
        echo("Missing required parameters\n");
        exit(1);
    }

    $endpoint = sprintf("%s/%s", $host, "status");
    $timestamp = (new DateTime())->format("c");
    $nonce = uniqid();
    echo sprintf("Calling with Timestamp [%s] and nonce [%s]\n", $timestamp, $nonce);

    # generate signature
    $payload = sprintf("date:%s\nx-mesh-nonce:%s", $timestamp, $nonce);
    $signature = hash_hmac("sha256", $payload, $secret, true);
    $signatureEncoded = base64_encode($signature);

    # setup a request
    $auth = sprintf(
        "HMAC-SHA256 Credential=%s;SignedHeaders=Date,x-mesh-nonce;Signature=%s",
        $key,
        $signatureEncoded);
    $ch = curl_init($endpoint);
    curl_setopt($ch, CURLOPT_HTTPHEADER, array(
        "Authorization: " . $auth,
        "Date: " . $timestamp,
        "x-mesh-nonce: " . $nonce
    ));
    curl_setopt($ch, CURLOPT_RETURNTRANSFER, 1);
    curl_setopt($ch, CURLOPT_HEADER, 1);
    curl_setopt($ch, CURLOPT_HEADERFUNCTION, function($curl, $header) use (&$conversation)
    {
        # find `x-mesh-conversation-id` header in response payload
        $len = strlen($header);
        $header = explode(':', $header, 2);
        if (count($header) < 2)
            return $len;
        if (strtolower(trim($header[0])) == "x-mesh-conversation-id") 
            $conversation = trim($header[1]);
        return $len;
    });

    # execute and get response
    echo sprintf("Calling %s...\n", $endpoint);
    $response = curl_exec($ch);
    $httpcode = curl_getinfo($ch, CURLINFO_HTTP_CODE);
    $header_size = curl_getinfo($ch, CURLINFO_HEADER_SIZE);
    $body = substr($response, $header_size);
    curl_close($ch);

    echo sprintf("Request ID [%s] returned [%s] with body:\n%s", $conversation, $httpcode, $body);
}

call_api(
    getenv("HOST"),
    getenv("API_KEY"),
    getenv("API_SECRET"));
echo("\n")
?>
