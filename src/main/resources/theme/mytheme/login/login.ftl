<#-- login-branch.ftl -->
<form id="kc-form-branch" action="${context.actionUrl}" method="post" class="form-inline justify-content-center">
    <div class="form-row align-items-center">
        <div class="col-auto mb-2">
            <label class="sr-only" for="branch">Branch</label>
            <input
                    type="text"
                    id="branch"
                    name="branch"
                    class="form-control form-control-sm"
                    placeholder="Branch"
                    required />
        </div>
        <div class="col-auto mb-2">
            <button type="submit" class="btn btn-primary btn-sm">
                ${label['doLogIn']!'Log In'}
            </button>
        </div>
    </div>
    <#if error?has_content>
        <div class="w-100 text-center text-danger small my-1">
            ${error}
        </div>
    </#if>
</form>
