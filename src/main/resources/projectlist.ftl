<#-- @ftlvariable name="projects" type="java.util.List<btrack.dao.ProjectBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список проектов</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
    <link href="/style.css" rel="stylesheet">
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
