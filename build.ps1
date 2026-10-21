param([switch]$Test, [switch]$Run)
$ErrorActionPreference = 'Stop'
Push-Location $PSScriptRoot
try {
    Get-Command javac, java -ErrorAction Stop | Out-Null
    $output = Join-Path $PSScriptRoot 'build/classes'
    New-Item -ItemType Directory -Path $output -Force | Out-Null
    $sources = @(Get-ChildItem -LiteralPath 'src' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName })
    if ($Test) { $sources += @(Get-ChildItem -LiteralPath 'tests' -Recurse -Filter '*.java' | ForEach-Object { $_.FullName }) }
    & javac -encoding UTF-8 -Xlint:all -Werror -d $output @sources
    if ($LASTEXITCODE -ne 0) { throw 'Falha na compilacao.' }
    if ($Test) {
        & java -ea '-Djava.awt.headless=true' -cp $output RegressionTest
        if ($LASTEXITCODE -ne 0) { throw 'Falha nos testes.' }
    }
    if ($Run) {
        & java -cp $output trab.Main
        if ($LASTEXITCODE -ne 0) { throw 'Falha na execucao.' }
    }
    Write-Host 'Compilacao concluida.'
} finally { Pop-Location }
