<#-- @ftlvariable name="bugs" type="java.util.List<btrack.dao.BugBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список багов</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
    <link href="/style.css" rel="stylesheet">
</head>
<body>
<#include "header.ftl">
<div class="container">
    <table>
        <tr>
            <th>Баг</th>
            <th>Описание</th>
            <th>Исполнитель</th>
            <th>Статус</th>
            <th>Создан</th>
            <th>Изменен</th>
        </tr>
        <#list bugs as b>
            <tr>
                <td><a href="${b.viewLink}">#${b.bugNum}</a></td>
                <td><a href="${b.viewLink}">${b.title}</a></td>
                <td>${b.assignedUser!}</td>
                <td>${b.state}</td>
                <td>${b.created}</td>
                <td>${b.lastUpdated}</td>
            </tr>
        </#list>
    </table>
</div>

</body>
</html>
