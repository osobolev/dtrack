<#-- @ftlvariable name="user" type="btrack.UserInfo" -->
<#-- @ftlvariable name="project" type="java.lang.String" -->
<nav class="navbar navbar-expand-lg navbar-dark bg-dark">
    <div class="navbar-collapse collapse">
        <ul class="navbar-nav mr-auto">
            <li class="nav-item active">
                <span class="navbar-text">Проект:</span>
            </li>
            <li class="nav-item active dropdown mr-3">
                <a class="nav-link dropdown-toggle" href="#" id="navbardrop" data-toggle="dropdown">${project}</a>
                <div class="dropdown-menu">
                    <a class="dropdown-item" href="#">Link 1</a>
                    <a class="dropdown-item" href="#">Link 2</a>
                    <a class="dropdown-item" href="#">Link 3</a>
                </div>
            </li>
            <li class="nav-item active mr-4">
                <a class="nav-link" href=""><u>Просмотр багов</u></a>
            </li>
            <li class="nav-item active">
                <button type="button" class="btn btn-primary" href="">Новый баг</button>
            </li>
        </ul>
        <ul class="navbar-nav">
            <li class="nav-item active mr-4">
                <span class="navbar-text">Пользователь: ${user.displayName}</span>
            </li>
            <li class="nav-item active">
                <form method="post" action="/logout.html">
                    <button type="submit" class="btn btn-secondary">Выход</button>
                </form>
            </li>
        </ul>
    </div>
</nav>
