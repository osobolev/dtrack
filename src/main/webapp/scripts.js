function configTable(elem, pageSize, what, columns) {
    var capWhat = what.charAt(0).toUpperCase() + what.substring(1);
    elem.DataTable({
        "language": {
            "decimal": "",
            "emptyTable": capWhat + " не найдено",
            "info": "Показано _START_ - _END_ из _TOTAL_ " + what,
            "infoEmpty": "Показано 0 - 0 из 0 " + what,
            "infoFiltered": "(из _MAX_ " + what + ")",
            "infoPostFix": "",
            "thousands": ",",
            "lengthMenu": "Показать _MENU_ " + what,
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
        "iDisplayLength": pageSize,
        "order": [],
        "autoWidth": false,
        "columns": columns
    });
}

function configSummer(elem, height) {
    elem.summernote({
        lang: 'ru-RU',
        height: height
    });
}

function filesChosen(filesElem, labelElem) {
    var fs = filesElem.get(0).files;
    var buf = '';
    for (var i = 0; i < fs.length; i++) {
        if (buf.length > 0) {
            buf += ', ';
        }
        buf += fs[i].name;
    }
    labelElem.text(buf);
}