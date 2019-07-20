<#-- @ftlvariable name="login" type="btrack.web.actions.LoginInfo" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список проектов</title>
    <#include "head.ftl">
</head>
<body>
<#include "header.ftl">
<div class="container pb-2 mt-2">
    <h3>Проекты</h3>
    <#list login.availableProjects as p>
        <h5 class="mt-4">
            <a href="${p.viewLink}">${p.name}</a>
        </h5>
        <#if p.description??>
            <small>${p.description}</small>
        </#if>
    </#list>
</div>

</body>
</html>
