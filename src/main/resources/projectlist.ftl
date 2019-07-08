<#-- @ftlvariable name="projects" type="java.util.List<btrack.dao.ProjectBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список проектов</title>
    <#include "head.ftl">
</head>
<body>
<#include "header.ftl">
<div class="container">
    <h1>Проекты</h1>
    <#list projects as p>
        <h3 class="mt-4">
            <a href="${p.viewLink}">${p.name}</a>
        </h3>
        <#if p.description??>
            <small>${p.description}</small>
        </#if>
    </#list>
</div>

</body>
</html>
