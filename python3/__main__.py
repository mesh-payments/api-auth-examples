import os
import sys
import requests
import datetime
import uuid
import hmac
import hashlib
import base64


def call_api(host: str, key: str, secret: str):
    # gather params
    timestamp = datetime.datetime.utcnow().replace(microsecond=0).isoformat() + 'Z'
    nonce = uuid.uuid4().hex
    print(f'Timestamp is {timestamp}, nonce is {nonce}')

    # generate signature
    payload = f'date:{timestamp}\nx-mesh-nonce:{nonce}'
    signature = hmac.new(
        bytes(secret, 'utf-8'),
        msg=bytes(payload, 'utf-8'),
        digestmod=hashlib.sha256).digest()
    signatureEncoded = base64.b64encode(signature).decode('utf-8')

    # make a request
    auth = f'HMAC-SHA256 Credential={key};SignedHeaders=Date,x-mesh-nonce;Signature={signatureEncoded}'
    response = requests.get(
        f'{host}/status',
        headers={
            'Authorization': auth,
            'Date': timestamp,
            'x-mesh-nonce': nonce
        }
    )
    requestId = response.headers['x-mesh-conversation-id']
    print(
        f'Request ID [{requestId}] returned [{response.status_code}]\n{response.text}')


if __name__ == '__main__':
    host = os.environ.get('HOST')
    key = os.environ.get('API_KEY')
    secret = os.environ.get('API_SECRET')
    if host is None or key is None or secret is None:
        raise ValueError('HOST, API_KEY or API_SECRET is missing')
    call_api(host, key, secret)
    sys.exit(0)
