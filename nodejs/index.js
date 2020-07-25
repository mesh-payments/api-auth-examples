'use strict';

const crypto = require('crypto-js');
const axios = require('axios');

const callApi = async (host, key, secret) => {
    // gather parameters
    const timestamp = new Date().toISOString();
    const nonce = Math.floor(Math.random() * 5);

    // generate signature
    const payload = `date:${timestamp}\nx-mesh-nonce:${nonce}`;
    const signature = crypto.HmacSHA256(payload, secret);
    const signatureEncoded = crypto.enc.Base64.stringify(signature);

    // call API
    const auth = `HMAC-SHA256 Credential=${key};SignedHeaders=Date,x-mesh-nonce;Signature=${signatureEncoded}`;
    const response = await axios.get(
        `${host}/status`,
        {
            headers: {
                Authorization: auth,
                Date: timestamp,
                'x-mesh-nonce': nonce
            }
        });

    console.log(`Request ID [${response.headers['x-mesh-conversation-id']}] returned [${response.status}]\n`, response.data);
};

const host = process.env.HOST;
const key = process.env.API_KEY;
const secret = process.env.API_SECRET;
if (!host || !key || !secret) {
    throw new Error('HOST, API_KEY or API_SECRET is missing');
}
callApi(host, key, secret);

