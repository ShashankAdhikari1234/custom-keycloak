<#-- error.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8" />
    <title>Error</title>
    <link rel="stylesheet" href="${url.resourcesPath}/css/login.css" />
</head>
<body>
<div class="error-container">
    <h2>An error occurred</h2>

    <#-- Display error message if available -->
    <#if errorMessage??>
        <div class="error-message">${errorMessage}</div>
    <#else>
        <div class="error-message">Unknown error occurred.</div>
    </#if>

    <p>
        <a href="${url.loginUrl}">Back to Login</a>
    </p>
</div>
</body>
</html>
