<#-- @ftlvariable name="report" type="btrack.web.data.ReportBean" -->
<#-- @ftlvariable name="bugGroups" type="java.util.List<java.util.List<btrack.web.data.BugBean>>" -->
<#-- @ftlvariable name="priorities" type="java.util.List<btrack.web.data.PriorityBean>" -->
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
        <#list priorities as p>
        .table-hover tbody tr.bugPriority${p.id} {
            background-color: ${p.color};
        }
        </#list>
    </style>
</head>
<body>
<#include "header.ftl">
<div class="container pb-2 mt-2">
    <h3>Отчет #${report.reportNum}: ${report.title}</h3>
    <#list bugGroups as bugs>
    <div class="pb-3"></div>
    <table id="bugs${bugs?counter}" class="table table-bordered table-hover" style="width: 100%;">
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
            <tr class="bugPriority${b.priorityCode}">
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
    </#list>
</div>

<script>
    $(document).ready(function () {
        <#list bugGroups as bugs>
        configTable($('#bugs${bugs?counter}'), 50, 'багов', [
            { "width": "8pt" },
            { "width" : "45%" },
            null,
            { "width": "50pt" },
            { "width": "80pt" },
            { "width": "80pt" },
        ]);
        </#list>
    });
</script>

</body>
</html>
