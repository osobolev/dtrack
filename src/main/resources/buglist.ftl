<#-- @ftlvariable name="report" type="btrack.dao.ReportBean" -->
<#-- @ftlvariable name="bugs" type="java.util.List<btrack.dao.BugBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список багов</title>
    <#include "head.ftl">
    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/dataTables.bootstrap4.min.css"/>
    <script type="text/javascript" src="DataTables-1.10.18/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="DataTables-1.10.18/js/dataTables.bootstrap4.min.js"></script>
    <style>
        .bugPriority1 {
            background-color: #fed;
        }
        .bugPriority2 {
            background-color: #ffb;
        }
        .bugPriority3 {
            background-color: #fbfbfb;
        }
    </style>
</head>
<body>
<#include "header.ftl">
<div class="container">
    <h2>Отчет #${report.reportNum}: ${report.title}</h2>
    <table id="bugs" class="table table-bordered table-hover" style="width: 100%;">
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
            <tr class="bugPriority${b.priorityId}">
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
            { "width" : "45%" },
            null,
            { "width": "50pt" },
            { "width": "80pt" },
            { "width": "80pt" },
        ]);
    });
</script>

</body>
</html>
