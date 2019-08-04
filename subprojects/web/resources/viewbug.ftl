<#-- @ftlvariable name="bug" type="btrack.web.data.BugBean" -->
<#-- @ftlvariable name="transitions" type="java.util.List<btrack.web.data.TransitionBean>" -->
<#-- @ftlvariable name="attachments" type="java.util.List<btrack.web.data.AttachmentBean>" -->
<#-- @ftlvariable name="changes" type="java.util.List<btrack.web.data.ChangeListBean>" -->
<#-- @ftlvariable name="users" type="java.util.List<btrack.web.data.UserBean>" -->
<#-- @ftlvariable name="error" type="java.lang.String" -->
<!DOCTYPE html>
<html lang="ru">
<head>
    <meta charset="UTF-8">
    <title>#${bug.bugNum} (${bug.title})</title>
    <#include "head.ftl">
    <link href="summernote-bs4.css" rel="stylesheet">
    <script src="summernote-bs4.js"></script>
    <script src="lang/summernote-ru-RU.js"></script>
</head>
<body>
<#include "header.ftl">
<div class="container pb-2">
    <div class="card mb-2">
        <div class="card-header">
            <h3 style="border-bottom: 1px solid gray; padding-bottom: 5px;">#${bug.bugNum}: ${bug.title}</h3>
            <#if error??>
                <h4 class="text-danger">${error}</h4>
            </#if>
            <div style="float: right;">
                <small>Создан ${bug.created} пользователем ${bug.createdBy}</small>
                <br>
                <small>Изменен ${bug.lastUpdated} пользователем ${bug.lastUpdatedBy}</small>
            </div>
            <div class="row align-items-center">
                <span class="ml-4 mr-2">Приоритет:</span>
                <button type="button" class="btn" style="background-color: ${bug.priorityColor};">${bug.priority}</button>

                <span class="ml-5 mr-2">Статус:</span>
                <span class="dropdown">
                    <button class="btn btn-primary dropdown-toggle" href="#" data-toggle="dropdown">${bug.state}</button>
                    <div class="dropdown-menu">
                        <#list transitions as t>
                            <a class="dropdown-item" href="javascript:void(0);" onclick="moveBug('${t.toCode}')">
                                ${t.name}
                            </a>
                        </#list>
                    </div>
                </span>

                <span class="ml-5 mr-2">Исполнитель:</span>
                <span class="dropdown">
                    <button class="btn btn-info dropdown-toggle" href="#" data-toggle="dropdown">
                        ${bug.assignedUser!"не выбран"}
                    </button>
                    <div class="dropdown-menu">
                        <#list users as u>
                            <a class="dropdown-item" href="javascript:void(0);" onclick="assignUser(${u.id})">
                                ${u.login}
                            </a>
                        </#list>
                        <#if bug.isAssigned()>
                            <a class="dropdown-item" href="javascript:void(0);" onclick="assignUser('')">
                                Сбросить
                            </a>
                        </#if>
                    </div>
                </span>

                <a class="btn btn-secondary ml-5" href="${bug.editLink}">Редактировать</a>
            </div>
        </div>
        <div class="card-body">
            ${bug.html?no_esc}
        </div>
        <#if attachments?has_content>
        <div class="card-footer">
            <ul style="margin-bottom: 0;">
                <#list attachments as a>
                    <li>
                        <a href="${bug.getAttachmentLink(a)}" target="_blank">${a.name}</a> (${a.size})
                    </li>
                </#list>
            </ul>
        </div>
        </#if>
    </div>

    <#if changes?has_content>
    <ul class="nav nav-tabs" role="tablist">
        <#list changes as change>
        <li class="nav-item">
            <a class="nav-link<#if change?is_first> active</#if>" data-toggle="tab" href="#${change.id}" role="tab">${change.text}</a>
        </li>
        </#list>
    </ul>

    <div class="tab-content border" style="padding-top: 5px; padding-bottom: 5px; margin-bottom: 5px;">
        <#list changes as change>
        <div id="${change.id}" class="tab-pane<#if change?is_first> show active</#if>" role="tabpanel">
            <#list change.changes as c>
            <div>
                <h6 class="change-heading">
                    <#if c.comments?has_content>
                        Комментарий пользователя ${c.user} ${c.ts}
                    <#else>
                        Изменено пользователем ${c.user} ${c.ts}
                    </#if>
                </h6>
                <#list c.comments as cc>
                <div class="change-comment">
                    <#if cc.deleted??>
                        <em>Комментарий удален пользователем ${cc.deleteUser} ${cc.deleted}</em>
                    <#else>
                        <form action="${cc.deleteLink}" method="post">
                            <input type="hidden" name="commentId" value="${cc.id}">
                            <button type="button" class="delete-comment-button" onclick="confirmDeleteComment(event)">Удалить
                            </button>
                        </form>
                        ${cc.commentHtml?no_esc}
                        <#if cc.commentAttachments?has_content>
                            Прикрепленные файлы:
                            <#list cc.commentAttachments as ca>
                                <a href="${bug.getCommentAttachmentLink(ca)}" target="_blank">${ca.name}</a> (${ca.size})<#if ca_has_next>, </#if>
                            </#list>
                        </#if>
                    </#if>
                </div>
                </#list>
                <ul class="change-other">
                    <#list c.details as cd>
                    <li>
                        <#if cd.fieldChange??>
                            ${cd.fieldChange}
                        <#elseif cd.fileChange??>
                            ${cd.fileChange}
                            <#list cd.changedFiles as cf>
                                <a href="${bug.getAttachmentLink(cf)}" target="_blank">${cf.name}</a> (${cf.size})<#if cf_has_next>, </#if>
                            </#list>
                        </#if>
                    </li>
                    </#list>
                </ul>
            </div>
            </#list>
        </div>
        </#list>
    </div>
    </#if>

    <a class="btn btn-secondary" data-toggle="collapse" href="#collapseComment" role="button">
        Новый комментарий
    </a>
    <div class="collapse mt-2" id="collapseComment">
        <form method="post" action="${bug.commentLink}" enctype="multipart/form-data">
            <div class="form-group">
                <textarea id="summernote" name="comment"></textarea>
            </div>
            <div class="form-group">
                <div class="custom-file">
                    <input type="file" class="custom-file-input" id="files" name="files" onchange="onFileChange()" multiple>
                    <label class="custom-file-label" for="files" id="filesLabel">Прикрепить файлы</label>
                </div>
            </div>
            <button type="submit" class="btn btn-primary">Добавить комментарий</button>
        </form>
    </div>
</div>

<script>
    $(document).ready(function () {
        configSummer($('#summernote'), '180px');
    });

    function onFileChange() {
        filesChosen($('#files'), $('#filesLabel'));
    }

    function onAssignedChange() {
        $('#assignForm').submit();
    }

    function confirmDeleteComment(e) {
        if (!confirm('Действительно удалить комментарий?'))
            return;
        var form = $(e.target).parent();
        form.submit();
    }

    function assignUser(newUserId) {
        var link = '${bug.assignLink}?oldUserId=${bug.assignedUserId}&newUserId=' + newUserId;
        $.post(link, function () {
            window.location.reload();
        });
    }

    function moveBug(newState) {
        var link = '${bug.moveLink}?from=${bug.stateCode}&to=' + newState;
        $.post(link, function () {
            window.location.reload();
        });
    }

    $('#collapseComment').on('shown.bs.collapse', function () {
        this.scrollIntoView();
        $('#summernote').summernote('focus');
    });
</script>

</body>
</html>
