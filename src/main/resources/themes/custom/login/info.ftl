<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Info</title>
    <style>
        body {
            font-family: sans-serif;
            padding: 2rem;
            text-align: center;
        }
        .message {
            color: #d33;
            font-size: 1.2rem;
        }
    </style>
</head>
<body>
<div class="message">
    <#if message??>
        ${message.summary}
    <#else>
        An unexpected error occurred.
    </#if>
</div>
</body>
</html>
