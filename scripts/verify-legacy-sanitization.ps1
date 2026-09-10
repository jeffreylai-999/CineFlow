# Verifies the legacy tree no longer contains known pre-sanitization secret/PII lines.
# Forbidden content is stored only as SHA-256 digests of trimmed lines.
$ErrorActionPreference = 'Stop'

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot '..')
$digestFile = Join-Path $PSScriptRoot 'legacy-forbidden-line-digests.txt'
$forbidden = Get-Content -LiteralPath $digestFile |
    Where-Object { $_.Trim() -ne '' } |
    ForEach-Object { $_.Trim().ToLowerInvariant() }

$sha = [System.Security.Cryptography.SHA256]::Create()
function Get-LineDigest([string]$text) {
    $bytes = [Text.Encoding]::UTF8.GetBytes($text)
    $hash = $sha.ComputeHash($bytes)
    return (($hash | ForEach-Object { $_.ToString('x2') }) -join '')
}

$scanRoots = @(
    (Join-Path $repoRoot 'legacy'),
    (Join-Path $repoRoot 'docs'),
    (Join-Path $repoRoot 'scripts'),
    (Join-Path $repoRoot 'CONTEXT.md'),
    (Join-Path $repoRoot 'AGENTS.md'),
    (Join-Path $repoRoot 'README.md'),
    (Join-Path $repoRoot '.gitignore')
)
$excludeDirs = [regex]'\\(\.git|derby-cinemas|nbproject\\private|\.superpowers|node_modules|build|dist)\\'
$extensions = @('.java', '.xml', '.sql', '.properties', '.jrxml', '.md', '.mf', '.gitignore')

$hits = @()
$files = foreach ($root in $scanRoots) {
    if (Test-Path -LiteralPath $root -PathType Leaf) {
        Get-Item -LiteralPath $root
    } elseif (Test-Path -LiteralPath $root -PathType Container) {
        Get-ChildItem -LiteralPath $root -Recurse -File |
            Where-Object { $extensions -contains $_.Extension.ToLowerInvariant() -or $_.Name -eq '.gitignore' }
    }
}

foreach ($file in $files) {
    if ($file.FullName -match $excludeDirs) { continue }
    if ($file.Name -eq 'legacy-forbidden-line-digests.txt') { continue }
    $relative = $file.FullName.Substring($repoRoot.Path.Length).TrimStart('\', '/')
    $lineNo = 0
    foreach ($line in Get-Content -LiteralPath $file.FullName) {
        $lineNo++
        $digest = Get-LineDigest $line.Trim()
        if ($forbidden -contains $digest) {
            $hits += "${relative}:${lineNo}"
        }
    }
}

if ($hits.Count -gt 0) {
    Write-Host "FAIL: found $($hits.Count) known pre-sanitization secret/PII line(s):"
    $hits | ForEach-Object { Write-Host "  $_" }
    exit 1
}

Write-Host 'OK: no known pre-sanitization secret/PII lines found.'
exit 0
