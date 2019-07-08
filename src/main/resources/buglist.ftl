<#-- @ftlvariable name="bugs" type="java.util.List<btrack.dao.BugBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Список багов</title>
    <#include "head.ftl">
    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/jquery.dataTables.min.css"/>
    <link rel="stylesheet" type="text/css" href="DataTables-1.10.18/css/dataTables.bootstrap4.min.css"/>
    <script type="text/javascript" src="DataTables-1.10.18/js/jquery.dataTables.min.js"></script>
    <script type="text/javascript" src="DataTables-1.10.18/js/dataTables.bootstrap4.min.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container">
    <table id="bugs" class="display" style="width: 100%;">
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
        $('#bugs').DataTable({
            "language": {
                "decimal": "",
                "emptyTable": "Багов не найдено",
                "info": "Показано _START_ - _END_ из _TOTAL_ багов",
                "infoEmpty": "Показано 0 - 0 из 0 багов",
                "infoFiltered": "(из _MAX_ багов)",
                "infoPostFix": "",
                "thousands": ",",
                "lengthMenu": "Показать _MENU_ багов",
                "loadingRecords": "Загрузка...",
                "processing": "Обработка...",
                "search": "Поиск:",
                "zeroRecords": "Ничего не найдено",
                "paginate": {
                    "first": "В начало",
                    "last": "В конец",
                    "next": "След",
                    "previous": "Пред"
                },
                "aria": {
                    "sortAscending": ": activate to sort column ascending",
                    "sortDescending": ": activate to sort column descending"
                }
            },
            "iDisplayLength": 50
        });
    });
</script>

</body>
</html>
