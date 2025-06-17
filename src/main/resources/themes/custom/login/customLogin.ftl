<#-- login.ftl -->
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8"/>
    <title>Custom Login</title>
</head>
<body style="font-family: Arial, sans-serif; background-color: #f5f7fa; margin: 0; padding: 0; display: flex; justify-content: center; align-items: center; height: 100vh;">

<div style="background: white; padding: 30px 40px; border-radius: 8px; box-shadow: 0 0 15px rgba(0,0,0,0.1); width: 320px;">
    <h2 style="text-align: center; color: #333; margin-bottom: 25px;">Smart Remittance V2</h2>

    <#-- Show error message if login failed -->
    <#if message?has_content>
        <div style="background-color: #f8d7da; color: #721c24; padding: 10px; margin-bottom: 15px; border-radius: 4px; border: 1px solid #f5c6cb;">
            ${message.summary}
        </div>
    </#if>

    <form action="${url.loginAction!''}" method="post" style="display: flex; flex-direction: column;">
        <label for="username" style="margin-bottom: 5px; font-weight: bold; color: #555;">Username/Email:</label>
        <input type="text" id="username" name="username" autofocus="autofocus" required
               style="padding: 8px 10px; margin-bottom: 15px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;"/>

        <label for="password" style="margin-bottom: 5px; font-weight: bold; color: #555;">Password:</label>
        <input type="password" id="password" name="password" required
               style="padding: 8px 10px; margin-bottom: 15px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;"/>

        <label for="branch" style="margin-bottom: 5px; font-weight: bold; color: #555;">Branch:</label>
        <input type="text" id="branch" name="branch" required
               style="padding: 8px 10px; margin-bottom: 20px; border: 1px solid #ccc; border-radius: 4px; font-size: 14px;"/>

        <input type="submit" value="Login"
               style="background-color: #007bff; color: white; padding: 10px; font-weight: bold; border: none; border-radius: 4px; cursor: pointer; transition: background-color 0.3s ease;"/>
    </form>
</div>

</body>
</html>
