<#-- @ftlvariable name="bug" type="btrack.data.BugBean" -->
<#-- @ftlvariable name="attachments" type="java.util.List<btrack.data.AttachmentBean>" -->
<#-- @ftlvariable name="priorities" type="java.util.List<btrack.data.PriorityBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование бага #${bug.bugNum}</title>
    <#include "head.ftl">
    <link href="summernote-bs4.css" rel="stylesheet">
    <script src="summernote-bs4.js"></script>
    <script src="lang/summernote-ru-RU.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container pb-2 mt-2">
    <h3>Баг #${bug.bugNum}</h3>
    <form method="post" action="${bug.editLink}" enctype="multipart/form-data">
        <div class="form-row">
            <div class="form-group col-md-10">
                <label for="title">Краткое описание:</label>
                <input type="text" class="form-control" id="title" name="title" placeholder="Краткое описание" value="${bug.title}">
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
            <textarea class="form-control" id="summernote" name="html">${bug.html?no_esc}</textarea>
        </div>
        <#if attachments?has_content>
            <ul>
                <#list attachments as a>
                    <li>
                        <input type="hidden" name="file_${a.id}" value="true">
                        <span class="bugFile"><a href="${bug.getAttachmentLink(a)}">${a.name}</a></span> (${a.size}) <a href="javascript:void(0)" class="small att-butt" onclick="removeAttachment(event)">Удалить</a>
                    </li>
                </#list>
            </ul>
        </#if>
        <div class="form-group">
            <div class="custom-file">
                <input type="file" class="custom-file-input" id="files" name="files" onchange="onFileChange()" multiple>
                <label class="custom-file-label" for="files" id="filesLabel">Добавить файлы</label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">Сохранить</button>
        <button type="button" class="btn btn-secondary" onclick="location.href='${bug.viewLink}'">Отмена</button>
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

    function removeAttachment(event) {
        var li = $(event.target).parent();
        var field = li.find('input[type=hidden]');
        var butt = li.find('.att-butt');
        var fname = li.find('span[class=bugFile]');
        if (field.val() === 'true') {
            fname.css('text-decoration', 'line-through');
            field.val('false');
            butt.text('Отменить');
        } else {
            fname.css('text-decoration', '');
            field.val('true');
            butt.text('Удалить');
        }
    }
</script>

</body>
</html>
