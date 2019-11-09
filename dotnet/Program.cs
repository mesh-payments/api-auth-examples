using System;
using System.Linq;
using System.Net.Http;
using System.Net.Http.Headers;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;

namespace MeshApi
{
    class Program
    {
        static void Main(string[] args)
        {
            var host = Environment.GetEnvironmentVariable("HOST") ?? throw new Exception("HOST is missing");
            var key = Environment.GetEnvironmentVariable("API_KEY") ?? throw new Exception("API_KEY is missing");
            var secret = Environment.GetEnvironmentVariable("API_SECRET") ?? throw new Exception("API_SECRET is missing");

            callApi(host, key, secret).GetAwaiter().GetResult();
        }

        private static async Task callApi(string host, string key, string secret)
        {
            // gather parameters
            var timestamp = DateTimeOffset.UtcNow;
            var nonce = Guid.NewGuid().ToString();

            // generate signature
            var payload = $"date:{timestamp.ToString("ddd, dd MMM yyyy HH:mm:ss 'GMT'")}\nx-mesh-nonce:{nonce}";
            using var hash = new HMACSHA256(Encoding.UTF8.GetBytes(secret));
            var signature = hash.ComputeHash(Encoding.UTF8.GetBytes(payload));
            var signatureEncoded = Convert.ToBase64String(signature);

            // make a request
            using var client = new HttpClient();
            client.DefaultRequestHeaders.Authorization = new AuthenticationHeaderValue(
                "HMAC-SHA256",
                $"Credential={key};SignedHeaders=Date,x-mesh-nonce;Signature={signatureEncoded}");
            client.DefaultRequestHeaders.Date = timestamp;
            client.DefaultRequestHeaders.Add("1x-mesh-nonce", nonce);

            // read response
            var response = await client.GetAsync($"{host}/status");
            var requestId = response.Headers.GetValues("x-amzn-RequestId").FirstOrDefault();
            var body = await response.Content.ReadAsStringAsync();
            Console.WriteLine($"Request ID [{requestId}] returned [{(int)response.StatusCode}]\n{body}\n");
        }
    }
}
