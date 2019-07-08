<#-- @ftlvariable name="bugs" type="java.util.List<btrack.dao.BugBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список багов</title>
    <#include "head.ftl">
<#--    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/jquery.dataTables.min.css"/>-->
    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/dataTables.bootstrap4.min.css"/>
    <script type="text/javascript" src="DataTables-1.10.18/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="DataTables-1.10.18/js/dataTables.bootstrap4.min.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container">
    <table id="bugs" class="display compact cell-border" style="width: 100%;">
        <thead>
        <tr>
            <th>Баг</th>
            <th>Описание</th>
            <th>Исполнитель</th>
            <th>Статус</th>
            <th>Создан</th>
            <th>Изменен</th>
        </tr>
        </thead>
        <tbody>
        <#list bugs as b>
            <tr>
                <td data-order="${b.bugNum}"><a href="${b.viewLink}">#${b.bugNum}</a></td>
                <td><a href="${b.viewLink}">${b.title}</a></td>
                <td>${b.assignedUser!}</td>
                <td>${b.state}</td>
                <td data-order="${b.createdISO}">${b.created}</td>
                <td data-order="${b.lastUpdatedISO}">${b.lastUpdated}</td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>

<script>
    $(document).ready(function () {
        configTable($('#bugs'), 50, 'багов', [
            { "width": "8pt" },
            { "width" : "50%" },
            null,
            { "width": "50pt" },
            { "width": "80pt" },
            { "width": "80pt" },
        ]);
    });
</script>

</body>
</html>
