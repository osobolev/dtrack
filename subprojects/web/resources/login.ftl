<#-- @ftlvariable name="error" type="java.lang.String" -->
<#-- @ftlvariable name="redirect" type="java.lang.String" -->
<#-- @ftlvariable name="login" type="java.lang.String" -->
<#-- @ftlvariable name="remember" type="boolean" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Вход</title>
    <#include "head.ftl">
</head>
<body>

<div class="container pt-5">
    <div class="col-md-6" style="float: none; margin: 0 auto;">
        <div>
            <h3>Вход</h3>
            <#if error??>
                <h4 class="text-danger">${error}</h4>
            </#if>
            <form method="post" action="login.html">
                <#if redirect??>
                    <input type="hidden" value="${redirect}" name="redirect">
                </#if>
                <div class="form-group">
                    <input class="form-control" placeholder="Логин" name="login" value="${login}">
                </div>
                <div class="form-group">
                    <input type="password" class="form-control" placeholder="Пароль" name="password">
                </div>
                <button type="submit" class="btn-primary form-control">Вход</button>
                <div class="mt-2" style="text-align: center;">
                    <input type="checkbox" name="rememberMe" style="vertical-align: middle;"<#if remember> checked</#if>>
                    <label class="form-check-label">Запомнить меня</label>
                </div>
            </form>
        </div>
    </div>
</div>

</body>
</html>
