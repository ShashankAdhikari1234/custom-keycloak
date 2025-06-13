<form ...>
    ${kcFormMessage("loginUsernameLabel", "Username")}
    <input name="username" id="username" />
    ${kcFormMessage("loginPasswordLabel", "Password")}
    <input type="password" name="password" id="password" />
    <label for="branch">Branch</label>
    <input name="branch" id="branch" required />
    <input type="submit" value="${label['doLogIn']}"/>
</form>
