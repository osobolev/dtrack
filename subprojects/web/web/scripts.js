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
        "stateSave": true,
        "stateSaveCallback": function (settings, data) {
            var order = data['order'];
            if (!order)
                return;
            var query = '';
            for (var i = 0; i < order.length; i++) {
                var col = order[i];
                var colIndex = col[0];
                var dir = col[1];
                query += '&' + colIndex + '=' + dir;
            }
            if (query.length > 0) {
                query = query.substring(1);
            }
            window.location.hash = '#' + query;
        },
        "stateLoadCallback": function (settings, callback) {
            var hash = window.location.hash;
            var orders = [];
            if (hash && hash.length > 1) {
                var parts = hash.substring(1).split('&');
                for (var i = 0; i < parts.length; i++) {
                    var part = parts[i].split('=');
                    if (part.length < 2)
                        continue;
                    var colIndex = parseInt(part[0]);
                    var order = part[1];
                    orders.push([colIndex, order]);
                }
            }
            return {
                "time": new Date().getTime(),
                "order": orders
            };
        },
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
