<#-- @ftlvariable name="bug" type="btrack.dao.BugBean" -->
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>Новый баг</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
    <link href="../summernote-bs4.css" rel="stylesheet">
    <script src="../summernote-bs4.js"></script>
    <script src="../lang/summernote-ru-RU.js"></script>
</head>
<body>
<div class="container-fluid">
    <div class="card">
        <div class="card-header">
            <h3>Проект ${bug.project} &ndash; баг #${bug.id}</h3>
        </div>
        <div class="card-body">
            <h5 class="card-title">${bug.title}</h5>
            <span>Создан ${bug.created} пользователем ${bug.createdBy}</span>
            ${bug.html?no_esc}
        </div>
    </div>
<#-- todo: priority -->
<#-- todo: state -->
<#-- todo: updated -->
<#-- todo: link to edit bug -->
<#-- todo: bug history -->
    <h5>Добавить комментарий:</h5>
    <form method="post" action="addcomment.html">
        <div class="form-group">
            <textarea id="summernote" name="comment"></textarea>
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
</script>

</body>
</html>
