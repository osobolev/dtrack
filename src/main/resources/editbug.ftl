<#-- @ftlvariable name="bug" type="btrack.dao.BugBean" -->
<#-- @ftlvariable name="attachments" type="java.util.List<btrack.dao.AttachmentBean>" -->
<#-- @ftlvariable name="priorities" type="java.util.List<btrack.dao.PriorityBean>" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>Редактирование бага #${bug.bugNum}</title>
    <link href="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/css/bootstrap.min.css" rel="stylesheet">
    <script src="http://cdnjs.cloudflare.com/ajax/libs/jquery/3.2.1/jquery.js"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/popper.js/1.15.0/umd/popper.min.js"></script>
    <script src="https://stackpath.bootstrapcdn.com/bootstrap/4.1.3/js/bootstrap.min.js"></script>
    <link href="/summernote-bs4.css" rel="stylesheet">
    <script src="/summernote-bs4.js"></script>
    <script src="/lang/summernote-ru-RU.js"></script>
</head>
<body>
<div class="container">
    <h3>Проект ${bug.project} &ndash; баг #${bug.bugNum}</h3>
    <form method="post" action="edit.html" enctype="multipart/form-data">
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
            <#-- todo: display state -->
            <#-- todo: display assigned user -->
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
                        <span class="bugFile">${a.name}</span><button type="button" onclick="removeAttachment(event)">Удалить</button>
                    </li>
                </#list>
            </ul>
        </#if>
        <div class="form-group">
            <div class="custom-file">
                <input type="file" class="custom-file-input" id="files" name="files" onchange="onFileChange()" multiple>
                <label class="custom-file-label" for="files" id="filesLabel">Прикрепить файлы</label>
            </div>
        </div>
        <button type="submit" class="btn btn-primary">Сохранить</button>
        <button type="button" class="btn" onclick="location.href='${bug.viewLink}'">Отмена</button>
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

    function removeAttachment(event) {
        var li = $(event.target).parent();
        var field = li.find('input[type=hidden]');
        var butt = li.find('button');
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
        console.log(field.val());
    }
</script>

</body>
</html>
