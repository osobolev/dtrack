<#-- @ftlvariable name="priorities" type="java.util.List<btrack.dao.PriorityBean>" -->
<#-- @ftlvariable name="project" type="java.lang.String" -->
<!DOCTYPE html>
<html lang="ru">
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
    <h3>Проект ${project} &ndash; новый баг</h3>
    <form method="post" action="newbug.html" enctype="multipart/form-data">
        <div class="form-row">
            <div class="form-group col-md-10">
                <label for="title">Краткое описание:</label>
                <input type="text" class="form-control" id="title" name="title" placeholder="Краткое описание">
            </div>
            <div class="form-group col-md-2">
                <label for="priority">Приоритет:</label>
                <select name="priority" id="priority" class="form-control">
                    <#list priorities as p>
                        <option value="${p.id}"<#if p.isDefault()> selected</#if>>${p.name}</option>
                    </#list>
                </select>
            </div>
        </div>
        <div class="form-group">
            <label for="summernote">Полное описание:</label>
            <textarea class="form-control" id="summernote" name="html"></textarea>
        </div>
        <div class="form-group row">
            <label for="files" class="col-md-2">Прикрепить файлы:</label>
            <div class="col-md-10">
                <input class="form-control" id="files" name="files" type="file" multiple>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">Создать</button>
    </form>
</div>

<script>
    $(document).ready(function () {
        $('#summernote').summernote({
            lang: 'ru-RU',
            height: '300px'
        });
        $('#title').focus();
    });
</script>

</body>
</html>
