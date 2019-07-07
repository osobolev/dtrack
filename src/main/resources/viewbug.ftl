<#-- @ftlvariable name="bug" type="btrack.dao.BugBean" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>#${bug.id} (${bug.title})</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
    <link href="../summernote-bs4.css" rel="stylesheet">
    <script src="../summernote-bs4.js"></script>
    <script src="../lang/summernote-ru-RU.js"></script>
</head>
<body>
<div class="container">
    <div class="card">
        <div class="card-header">
            <h3>#${bug.id}: ${bug.title}</h3>
            <div style="float: right;">
                <small>Создан ${bug.created} пользователем ${bug.createdBy}</small>
                <br>
                <small>Изменен ${bug.lastUpdated} пользователем ${bug.lastUpdatedBy}</small>
            </div>
            Приоритет: ${bug.priority}
            <a href="edit.html">Редактировать</a>
            Состояние: ${bug.state}
        <#-- todo: priority -->
        <#-- todo: state + move buttons -->
        <#-- todo: assigned user -->
        <#-- todo: link to edit bug -->
        </div>
        <div class="card-body">
            ${bug.html?no_esc}
        </div>
    </div>
    <#-- todo: bug history -->
    <h5>Добавить комментарий:</h5>
    <form method="post" action="addcomment.html">
        <div class="form-group">
            <textarea id="summernote" name="comment"></textarea>
        </div>
        <div class="form-group">
            <div class="custom-file">
                <input type="file" class="custom-file-input" id="files" onchange="onFileChange()" multiple>
                <label class="custom-file-label" for="files" id="filesLabel">Добавить файлы</label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">Добавить комментарий</button>
    </form>
</div>

<script>
    $(document).ready(function () {
        $('#summernote').summernote({
            lang: 'ru-RU',
            height: '200px'
        });
    });

    function onFileChange() {
        var fs = $('#files').get(0).files;
        var buf = '';
        for (var i = 0; i < fs.length; i++) {
            if (buf.length > 0) {
                buf += ', ';
            }
            buf += fs[i].name;
        }
        $('#filesLabel').text(buf);
    }
</script>

</body>
</html>
