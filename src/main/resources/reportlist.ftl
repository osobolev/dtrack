<#-- @ftlvariable name="reports" type="java.util.List<btrack.dao.ReportBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список отчетов</title>
    <#include "head.ftl">
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
