<#-- @ftlvariable name="info" type="btrack.actions.CommonInfo" -->
<#-- @ftlvariable name="displayUser" type="java.lang.String" -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="navbar-collapse collapse">
        <ul class="navbar-nav mr-auto">
            <#if info??>
            <li class="nav-item">
                <span class="navbar-text">Проект:</span>
            </li>
            <li class="nav-item active dropdown mr-3">
                <a class="nav-link dropdown-toggle" href="#" data-toggle="dropdown">${info.projectName}</a>
                <div class="dropdown-menu">
                    <#list info.availableProjects as p>
                        <a class="dropdown-item" href="${p.viewLink}">${p.name}</a>
                    </#list>
                </div>
            </li>
            <li class="nav-item active mr-4">
                <a class="nav-link" href="${info.reportRootUrl}"><u>Просмотр багов</u></a>
                <#-- todo: show dropdown with available reports for quick navigation??? -->
            </li>
            <li class="nav-item active">
                <button type="button" class="btn btn-primary" onclick="location.href='${info.newBugUrl}';">Новый баг</button>
            </li>
            <#else>
            <li class="nav-item">
                <span class="navbar-text">Выбор проекта</span>
            </li>
            </#if>
        </ul>
        <ul class="navbar-nav">
            <li class="nav-item active mr-4">
                <span class="navbar-text">Пользователь: ${displayUser}</span>
            </li>
            <li class="nav-item active">
                <form method="post" action="logout.html">
                    <button type="submit" class="btn btn-secondary">Выход</button>
                </form>
            </li>
        </ul>
    </div>
</nav>
