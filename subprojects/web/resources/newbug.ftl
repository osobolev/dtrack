<#-- @ftlvariable name="postLink" type="java.lang.String" -->
<#-- @ftlvariable name="priorities" type="java.util.List<btrack.web.data.PriorityBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Новый баг</title>
    <#include "head.ftl">
    <link href="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.12/summernote-bs4.css" rel="stylesheet">
    <script src="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.12/summernote-bs4.min.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/summernote/0.8.12/lang/summernote-ru-RU.min.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container pb-2 mt-2">
    <h3>Новый баг</h3>
    <form method="post" action="${postLink}" enctype="multipart/form-data">
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
        <div class="form-group">
            <div class="custom-file">
                <input type="file" class="custom-file-input" id="files" name="files" onchange="onFileChange()" multiple>
                <label class="custom-file-label" for="files" id="filesLabel">Прикрепить файлы</label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">Создать</button>
    </form>
</div>

<script>
    $(document).ready(function () {
        configSummer($('#summernote'), '300px');
        $('#title').focus();
    });

    function onFileChange() {
        filesChosen($('#files'), $('#filesLabel'));
    }
</script>

</body>
</html>
