<#-- @ftlvariable name="stats" type="java.util.List<btrack.web.data.StatsBean>" -->
<#-- @ftlvariable name="reports" type="java.util.List<btrack.web.data.ReportBean>" -->
<#-- @ftlvariable name="project" type="btrack.web.actions.ProjectInfo" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список отчетов</title>
    <#include "head.ftl">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.18/css/dataTables.bootstrap4.min.css" rel="stylesheet"/>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.18/js/jquery.dataTables.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/datatables/1.10.18/js/dataTables.bootstrap4.min.js"></script>
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
                <td data-order="${r.reportNum}">
                    <span class="align-middle">
                    <img src="<#if project.isFavourite(r)>star.png<#else>no_star.png</#if>" alt="star" style="vertical-align: text-top; margin-right: 3px; cursor: pointer;" onclick="favourite(event, '${r.viewLink}')">
                    <a href="${r.viewLink}">#${r.reportNum}</a>
                    </span>
                </td>
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

    function favourite(event, reportLink) {
        const src = $(event.target).attr('src');
        let page;
        if (src === 'star.png') {
            $(event.target).attr('src', 'no_star.png');
            page = 'unlike.html';
        } else {
            $(event.target).attr('src', 'star.png');
            page = 'like.html';
        }
        $.post(reportLink + '/' + page);
    }
</script>

</body>
</html>
