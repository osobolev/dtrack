<#-- @ftlvariable name="reports" type="java.util.List<btrack.dao.ReportBean>" -->
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
    <h2>Отчеты</h2>
    <table>
        <tr>
            <th>Отчет</th>
            <th>Описание</th>
        </tr>
        <#list reports as r>
            <tr>
                <td><a href="${r.viewLink}">#${r.reportNum}</a></td>
                <td><a href="${r.viewLink}">${r.title}</a></td>
            </tr>
        </#list>
    </table>
</div>

</body>
</html>
