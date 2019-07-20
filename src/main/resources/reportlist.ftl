<#-- @ftlvariable name="stats" type="java.util.List<btrack.dao.StatsBean>" -->
<#-- @ftlvariable name="reports" type="java.util.List<btrack.dao.ReportBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список отчетов</title>
    <#include "head.ftl">
    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/dataTables.bootstrap4.min.css"/>
    <script type="text/javascript" src="DataTables-1.10.18/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="DataTables-1.10.18/js/dataTables.bootstrap4.min.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container pb-2 mt-2">
    <h3>Список отчетов</h3>
    <div class="pb-3">
        <#list stats as s>
            <span class="badge badge-info">${s.state}: ${s.count}</span>
        </#list>
    </div>
    <table id="reports" class="table table-bordered table-hover" style="width: 100%;">
        <thead>
        <tr>
            <th>Отчет</th>
            <th>Описание</th>
        </tr>
        </thead>
        <tbody>
        <#list reports as r>
            <tr>
                <td data-order="${r.reportNum}"><a href="${r.viewLink}">#${r.reportNum}</a></td>
                <td><a href="${r.viewLink}">${r.title}</a></td>
            </tr>
        </#list>
        </tbody>
    </table>
</div>

<script>
    $(document).ready(function () {
        configTable($('#reports'), 10, 'отчетов', [
            { "width": "8pt" },
            null
        ]);
    });
</script>

</body>
</html>
