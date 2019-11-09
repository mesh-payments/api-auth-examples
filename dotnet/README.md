# Calling Mesh API with C#

Assuming that you have [.NET core](https://dotnet.microsoft.com/download) installed, run the example from this directory:

```bash
HOST={YOUR HOST} API_KEY={YOUR KEY} API_SECRET={YOUR SECRET} dotnet run
```

If you on Windows, then run from Powershell:

```powershell
$env:HOST = '{YOUR HOST}'; $env:API_KEY = '{YOUR KEY}'; $env:API_SECRET = '{YOUR SECRET}'; `
    dotnet run; Remove-Item Env:\HOST; Remove-Item Env:\API_KEY; Remove-Item Env:\API_SECRET
```
