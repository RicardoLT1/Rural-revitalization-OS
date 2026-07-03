const DEFAULT_BASE_URL = "http://127.0.0.1:8080/api";

const state = {
  baseUrl: DEFAULT_BASE_URL,
  token: localStorage.getItem("xiangyun.token") || "",
  user: readJson(localStorage.getItem("xiangyun.user")),
  view: localStorage.getItem("xiangyun.view") || "dashboard",
  days: "",
  todoKeyword: "",
  todoStatus: "PENDING",
  approvalKeyword: "",
  approvalStatus: "ALL",
  resourceKeyword: "",
  resourceCategory: "ALL",
  resourceStatus: "ALL",
  userKeyword: "",
  userRole: "ALL",
  isLoggingIn: false,
  authMode: "login",
};

const els = {
  loginPage: document.querySelector("#loginPage"),
  appShell: document.querySelector("#appShell"),
  authTitle: document.querySelector(".auth-heading h2"),
  authSubtitle: document.querySelector(".auth-heading p"),
  registerDisplayName: document.querySelector("#registerDisplayName"),
  displayNameField: document.querySelector("#displayNameField"),
  username: document.querySelector("#username"),
  password: document.querySelector("#password"),
  confirmPassword: document.querySelector("#confirmPassword"),
  confirmPasswordField: document.querySelector("#confirmPasswordField"),
  passwordToggle: document.querySelector("#passwordToggle"),
  rememberMe: document.querySelector("#rememberMe"),
  submitAuthBtn: document.querySelector("#submitAuthBtn"),
  authModeToggle: document.querySelector("#authModeToggle"),
  authSwitchText: document.querySelector("#authSwitchText"),
  logoutBtn: document.querySelector("#logoutBtn"),
  refreshBtn: document.querySelector("#refreshBtn"),
  formMessage: document.querySelector("#formMessage"),
  authCard: document.querySelector("#authCard"),
  sessionPanel: document.querySelector("#sessionPanel"),
  displayName: document.querySelector("#displayName"),
  roleBadge: document.querySelector("#roleBadge"),
  topAvatar: document.querySelector("#topAvatar"),
  topRole: document.querySelector("#topRole"),
  nav: document.querySelector("#nav"),
  eyebrow: document.querySelector("#eyebrow"),
  pageTitle: document.querySelector("#pageTitle"),
  content: document.querySelector("#content"),
  detailDrawer: document.querySelector("#detailDrawer"),
  detailTitle: document.querySelector("#detailTitle"),
  detailContent: document.querySelector("#detailContent"),
};

const NAV = [
  { key: "dashboard", label: "数据看板", roles: ["STAFF", "ADMIN"] },
  { key: "todos", label: "待办审批", roles: ["STAFF", "ADMIN"] },
  { key: "resources", label: "资源列表", roles: ["STAFF", "ADMIN"] },
  { key: "approvals", label: "审批历史", roles: ["STAFF", "ADMIN"] },
  { key: "users", label: "用户管理", roles: ["ADMIN"] },
  { key: "system", label: "系统信息", roles: ["ADMIN"] },
];

bindEvents();
restoreSession();

function bindEvents() {
  els.submitAuthBtn.addEventListener("click", submitAuth);
  els.logoutBtn.addEventListener("click", logout);
  els.refreshBtn.addEventListener("click", () => renderCurrentView());
  els.passwordToggle.addEventListener("click", togglePasswordVisibility);
  els.authModeToggle.addEventListener("click", toggleAuthMode);

  [els.registerDisplayName, els.username, els.password, els.confirmPassword].forEach((input) => {
    input.addEventListener("keydown", (event) => {
      if (event.key === "Enter") submitAuth();
    });
  });

  els.nav.addEventListener("click", (event) => {
    const button = event.target.closest("[data-view]");
    if (!button) return;
    state.view = button.dataset.view;
    localStorage.setItem("xiangyun.view", state.view);
    renderShell();
    renderCurrentView();
  });

  els.content.addEventListener("click", async (event) => {
    const dashboardRangeButton = event.target.closest("[data-dashboard-days]");
    if (dashboardRangeButton) {
      state.days = dashboardRangeButton.dataset.dashboardDays;
      await renderDashboard();
      return;
    }
    const approvalButton = event.target.closest("[data-approval]");
    if (approvalButton) {
      await handleApproval(approvalButton.dataset.workflowId, approvalButton.dataset.approval);
      return;
    }
    const detailButton = event.target.closest("[data-detail]");
    if (detailButton) {
      await openWorkflowDetail(detailButton.dataset.detail);
      return;
    }
    const resourceButton = event.target.closest("[data-resource-action]");
    if (resourceButton) {
      await handleResourceAction(resourceButton.dataset.resourceAction, resourceButton.dataset.resourceId);
      return;
    }
    const userButton = event.target.closest("[data-user-action]");
    if (userButton) {
      await handleUserAction(userButton.dataset.userAction, userButton.dataset.userId);
    }
  });

  els.content.addEventListener("input", (event) => {
    if (event.target.id === "todoKeyword") state.todoKeyword = event.target.value;
    if (event.target.id === "approvalKeyword") state.approvalKeyword = event.target.value;
    if (event.target.id === "resourceKeyword") state.resourceKeyword = event.target.value;
    if (event.target.id === "userKeyword") state.userKeyword = event.target.value;
  });

  els.content.addEventListener("change", (event) => {
    if (event.target.id === "todoStatus") {
      state.todoStatus = event.target.value;
      renderTodos();
    }
    if (event.target.id === "approvalStatus") {
      state.approvalStatus = event.target.value;
      renderApprovals();
    }
    if (event.target.id === "resourceCategory") {
      state.resourceCategory = event.target.value;
      renderResources();
    }
    if (event.target.id === "resourceStatus") {
      state.resourceStatus = event.target.value;
      renderResources();
    }
    if (event.target.id === "userRole") {
      state.userRole = event.target.value;
      renderUsers();
    }
  });

  document.body.addEventListener("click", (event) => {
    if (event.target.closest("[data-close-detail]")) {
      closeWorkflowDetail();
    }
  });
}

async function restoreSession() {
  if (!state.token) {
    renderShell();
    return;
  }
  try {
    const result = await api("/auth/me");
    state.user = result.body.data;
    saveSession();
  } catch (error) {
    clearSession();
    setMessage(error.message, true);
  }
  renderShell();
  renderCurrentView();
}

async function submitAuth() {
  if (state.authMode === "register") {
    await register();
    return;
  }
  await login();
}

async function login() {
  if (state.isLoggingIn) return;
  const username = els.username.value.trim();
  const password = els.password.value;
  if (!username || !password) {
    setMessage("请输入账号和密码", true);
    markLoginInvalid(true);
    return;
  }

  state.isLoggingIn = true;
  markLoginInvalid(false);
  setLoginButtonLoading(true);
  setMessage("");
  try {
    const result = await api("/auth/login", {
      method: "POST",
      body: {
        username,
        password,
      },
      skipAuth: true,
    });
    acceptLogin(result.body.data);
  } catch (error) {
    markLoginInvalid(true);
    setMessage(toFriendlyError(error.message, "账号或密码错误，请重新输入"), true);
  } finally {
    state.isLoggingIn = false;
    setLoginButtonLoading(false);
  }
}

async function register() {
  if (state.isLoggingIn) return;
  const displayName = els.registerDisplayName.value.trim();
  const username = els.username.value.trim();
  const password = els.password.value;
  const confirmPassword = els.confirmPassword.value;
  if (!displayName || !username || !password || !confirmPassword) {
    setMessage("请完整填写注册信息", true);
    markLoginInvalid(true);
    return;
  }
  if (username.length < 3) {
    setMessage("账号至少 3 个字符", true);
    markLoginInvalid(true);
    return;
  }
  if (password.length < 6) {
    setMessage("密码至少 6 位", true);
    markLoginInvalid(true);
    return;
  }
  if (password !== confirmPassword) {
    setMessage("两次输入的密码不一致", true);
    markLoginInvalid(true);
    return;
  }

  state.isLoggingIn = true;
  markLoginInvalid(false);
  setLoginButtonLoading(true);
  setMessage("");
  try {
    const result = await api("/auth/register", {
      method: "POST",
      body: {
        username,
        password,
        displayName,
      },
      skipAuth: true,
    });
    acceptLogin(result.body.data);
  } catch (error) {
    markLoginInvalid(true);
    setMessage(toFriendlyError(error.message, "注册失败，请检查账号信息"), true);
  } finally {
    state.isLoggingIn = false;
    setLoginButtonLoading(false);
  }
}

function acceptLogin(data) {
  state.token = data.token;
  state.user = data.user;
  if (state.user.role === "USER") {
    state.view = "forbidden";
    saveSession();
    renderShell();
    renderCurrentView();
    setMessage("当前账号无权进入管理工作台", true);
    return;
  }
  state.view = state.user.role === "USER" ? "forbidden" : "dashboard";
  saveSession();
  setMessage("");
  renderShell();
  renderCurrentView();
}

async function logout() {
  try {
    if (state.token) await api("/auth/logout", { method: "POST" });
  } catch (error) {
    console.warn(error);
  }
  clearSession();
  renderShell();
  setMessage("");
  els.content.innerHTML = "";
}

function renderShell() {
  const hasSession = Boolean(state.token && state.user);
  els.loginPage.classList.toggle("hidden", hasSession);
  els.appShell.classList.toggle("hidden", !hasSession);
  els.sessionPanel.classList.toggle("hidden", !hasSession);
  els.nav.classList.toggle("hidden", !hasSession || state.user.role === "USER");
  els.refreshBtn.classList.toggle("hidden", !hasSession || state.user.role === "USER");

  if (!hasSession) {
    if (els.content) els.content.innerHTML = "";
    return;
  }

  els.displayName.textContent = state.user.displayName || state.user.username;
  els.roleBadge.textContent = state.user.role;
  els.topAvatar.textContent = (state.user.displayName || state.user.username || "A").slice(0, 1).toUpperCase();
  els.topRole.textContent = state.user.role;
  els.eyebrow.textContent = `${state.user.username} / ${state.user.role}`;

  if (state.user.role === "USER") {
    els.pageTitle.textContent = "无访问权限";
    els.nav.innerHTML = "";
    return;
  }

  const views = availableViews();
  if (!views.some((item) => item.key === state.view)) state.view = views[0].key;
  els.nav.innerHTML = views
    .map((item) => `<button type="button" class="${item.key === state.view ? "active" : ""}" data-view="${item.key}">${item.label}</button>`)
    .join("");
  els.pageTitle.textContent = views.find((item) => item.key === state.view)?.label || "综合工作台";
}

async function renderCurrentView() {
  if (!state.token || !state.user) return;
  if (state.user.role === "USER" || state.view === "forbidden") {
    renderForbidden();
    return;
  }
  setLoading();
  try {
    if (state.view === "dashboard") await renderDashboard();
    if (state.view === "todos") await renderTodos();
    if (state.view === "resources") await renderResources();
    if (state.view === "approvals") await renderApprovals();
    if (state.view === "users") await renderUsers();
    if (state.view === "system") renderSystem();
  } catch (error) {
    renderError(error.message);
  }
}

async function renderDashboard() {
  const query = state.days ? `?days=${encodeURIComponent(state.days)}` : "";
  const result = await api(`/dashboard${query}`);
  const data = result.body.data || {};
  const stats = Array.isArray(data.stats) ? data.stats : [];
  const cacheStatus = result.response.headers.get("X-Cache-Status") || "-";
  const trends7 = data.trends?.days7 || [];
  const trends30 = data.trends?.days30 || trends7;
  const activeDays = state.days || String(data.rangeDays || 7);
  const safeStats = stats.length ? stats : [{ title: "快照数量", value: data.snapshotCount || 0, unit: "" }];
  els.content.innerHTML = `
    <div class="toolbar dashboard-toolbar">
      <div class="filters">
        <label class="date-filter">
          <span>统计范围</span>
          <input id="daysInput" inputmode="numeric" placeholder="默认天数" value="${escapeHtml(state.days)}" />
        </label>
        <button class="small-btn" type="button" id="applyDays">应用</button>
        <div class="segmented-control" aria-label="看板时间范围">
          <button class="${activeDays === "1" ? "active" : ""}" type="button" data-dashboard-days="1">今天</button>
          <button class="${activeDays === "7" ? "active" : ""}" type="button" data-dashboard-days="7">近7天</button>
          <button class="${activeDays === "30" ? "active" : ""}" type="button" data-dashboard-days="30">近30天</button>
        </div>
      </div>
      <span class="status">缓存：${escapeHtml(cacheStatus)}</span>
    </div>
    <div class="kpi-grid">
      ${safeStats.slice(0, 4).map((item, index) => renderKpi(item.title, `${item.value}${item.unit || ""}`, index)).join("")}
    </div>
    <div class="dashboard-grid">
      <div class="data-panel visual-panel trend-card span-2">
        <div class="panel-head">
          <div>
            <h3>运营趋势</h3>
            <p>近 ${escapeHtml(activeDays)} 天资源访问与合作热度</p>
          </div>
          <div class="panel-tabs"><button class="active" type="button">面积图</button><button type="button">折线图</button></div>
        </div>
        ${renderTrendVisual(activeDays === "30" ? trends30 : trends7)}
      </div>
      <div class="data-panel visual-panel">
        <div class="panel-head">
          <div>
            <h3>资源分类分布</h3>
            <p>资源结构概览</p>
          </div>
          <button class="mini-link" type="button">更多</button>
        </div>
        ${renderCategoryDonut(safeStats)}
      </div>
      <div class="data-panel visual-panel">
        <div class="panel-head">
          <div>
            <h3>审批状态分布</h3>
            <p>流程处理结构</p>
          </div>
          <button class="mini-link" type="button">更多</button>
        </div>
        ${renderApprovalBars(safeStats)}
      </div>
      <div class="data-panel visual-panel">
        <div class="panel-head">
          <div>
            <h3>近 7 日热力图</h3>
            <p>访问、咨询、申请、审批</p>
          </div>
        </div>
        ${renderHeatmap(trends7)}
      </div>
      <div class="data-panel visual-panel">
        <div class="panel-head">
          <div>
            <h3>资源活跃度 TOP5</h3>
            <p>根据访问和申请综合排序</p>
          </div>
        </div>
        ${renderActivityTop(trends7)}
      </div>
    </div>
    <div class="data-panel">
      <div class="panel-head compact-head">
        <div>
          <h3>趋势明细</h3>
          <p>用于核对看板图表来源</p>
        </div>
        <button class="small-btn" type="button">导出</button>
      </div>
      ${renderTrendTable(activeDays === "30" ? trends30 : trends7)}
    </div>
  `;
  document.querySelector("#applyDays").addEventListener("click", () => {
    state.days = document.querySelector("#daysInput").value.trim();
    renderDashboard();
  });
}

async function renderTodos() {
  const result = await api("/workflows/todos");
  const rows = filterRows(Array.isArray(result.body.data) ? result.body.data : [], state.todoKeyword, state.todoStatus);
  els.content.innerHTML = `
    <div class="toolbar">
      <div class="filters">
        <input id="todoKeyword" placeholder="搜索标题或编号" value="${escapeHtml(state.todoKeyword)}" />
        <select id="todoStatus">
          ${renderStatusOption("PENDING", "待审批", state.todoStatus)}
          ${renderStatusOption("APPROVED", "已通过", state.todoStatus)}
          ${renderStatusOption("REJECTED", "已驳回", state.todoStatus)}
          ${renderStatusOption("ALL", "全部状态", state.todoStatus)}
        </select>
        <button class="small-btn" type="button" onclick="renderTodos()">查询</button>
      </div>
    </div>
    <div class="data-panel">
      <h3>待办审批</h3>
      ${rows.length ? `
        <table class="dense-table">
          <thead><tr><th>流程</th><th>类型</th><th>状态</th><th>截止时间</th><th>操作</th></tr></thead>
          <tbody>
            ${rows.map((row) => `
              <tr>
                <td>${escapeHtml(displayText(row.title, defaultWorkflowTitle(row)))}<div class="muted">ID: ${escapeHtml(row.processId || row.workflowId || row.id)}</div></td>
                <td>${escapeHtml(displayCategory(row.category))}</td>
                <td>${renderStatus(row.status)}</td>
                <td>${escapeHtml(formatDate(row.dueDate))}</td>
                <td>
                  <div class="row-actions">
                    <button class="small-btn" type="button" data-detail="${escapeHtml(row.processId || row.workflowId || row.id)}">详情</button>
                    ${renderApprovalActions(row.processId || row.workflowId || row.id, row.status)}
                  </div>
                </td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      ` : renderEmpty("暂无待办")}
    </div>
  `;
}

async function renderResources() {
  const params = new URLSearchParams({ page: "1", size: "50" });
  if (state.resourceKeyword.trim()) params.set("keyword", state.resourceKeyword.trim());
  if (state.resourceCategory !== "ALL") params.set("category", state.resourceCategory);
  if (state.resourceStatus !== "ALL") params.set("investmentStatus", state.resourceStatus);
  const result = await api(`/resources?${params.toString()}`);
  const rows = Array.isArray(result.body.data) ? result.body.data : [];
  els.content.innerHTML = `
    <div class="toolbar">
      <div class="filters">
        <input id="resourceKeyword" placeholder="搜索资源名称" value="${escapeHtml(state.resourceKeyword)}" />
        <select id="resourceCategory">
          ${renderStatusOption("ALL", "全部类型", state.resourceCategory)}
          ${renderStatusOption("闲置农房", "闲置农房", state.resourceCategory)}
          ${renderStatusOption("土地", "土地", state.resourceCategory)}
          ${renderStatusOption("文旅空间", "文旅空间", state.resourceCategory)}
        </select>
        <select id="resourceStatus">
          ${renderStatusOption("ALL", "全部招商状态", state.resourceStatus)}
          ${renderStatusOption("可招商", "可招商", state.resourceStatus)}
          ${renderStatusOption("洽谈中", "洽谈中", state.resourceStatus)}
          ${renderStatusOption("已签约", "已签约", state.resourceStatus)}
          ${renderStatusOption("已下架", "已下架", state.resourceStatus)}
        </select>
        <button class="small-btn" type="button" onclick="renderResources()">查询</button>
      </div>
      ${state.user.role === "ADMIN" ? `<button class="primary compact" type="button" data-resource-action="create">新增资源</button>` : ""}
    </div>
    <div class="data-panel">
      <h3>资源列表</h3>
      ${rows.length ? `
        <table>
          <thead><tr><th>资源</th><th>类型</th><th>面积</th><th>年收益预估</th><th>招商状态</th><th>操作</th></tr></thead>
          <tbody>
            ${rows.map((row) => `
              <tr>
                <td>${escapeHtml(displayText(row.name, "乡村资源"))}<div class="muted">${escapeHtml(displayText(row.address || row.owner, ""))}</div></td>
                <td>${escapeHtml(displayCategory(row.category))}</td>
                <td>${escapeHtml(row.area || "-")}</td>
                <td>${escapeHtml(row.annualEstimate || "-")}</td>
                <td>${renderStatus(row.investmentStatus || "-")}</td>
                <td>
                  <div class="row-actions">
                    <button class="small-btn" type="button" data-resource-action="detail" data-resource-id="${escapeHtml(row.id)}">详情</button>
                    ${state.user.role === "ADMIN" ? `
                      <button class="small-btn" type="button" data-resource-action="edit" data-resource-id="${escapeHtml(row.id)}">编辑</button>
                      <button class="small-btn approve" type="button" data-resource-action="publish" data-resource-id="${escapeHtml(row.id)}">发布</button>
                      <button class="small-btn reject" type="button" data-resource-action="offline" data-resource-id="${escapeHtml(row.id)}">下架</button>
                      <button class="small-btn" type="button" data-resource-action="status" data-resource-id="${escapeHtml(row.id)}">招商状态</button>
                    ` : ""}
                  </div>
                </td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      ` : renderEmpty("暂无资源")}
    </div>
  `;
}

async function renderApprovals() {
  const result = await api("/workflows/approvals");
  const rows = filterRows(Array.isArray(result.body.data) ? result.body.data : [], state.approvalKeyword, state.approvalStatus);
  els.content.innerHTML = `
    <div class="toolbar">
      <div class="filters">
        <input id="approvalKeyword" placeholder="搜索标题或流程编号" value="${escapeHtml(state.approvalKeyword)}" />
        <select id="approvalStatus">
          ${renderStatusOption("ALL", "全部状态", state.approvalStatus)}
          ${renderStatusOption("APPROVED", "已通过", state.approvalStatus)}
          ${renderStatusOption("REJECTED", "已驳回", state.approvalStatus)}
        </select>
        <button class="small-btn" type="button" onclick="renderApprovals()">查询</button>
      </div>
    </div>
    <div class="data-panel">
      <h3>审批历史</h3>
      ${rows.length ? `
        <table>
          <thead><tr><th>标题</th><th>流程</th><th>动作</th><th>状态</th><th>时间</th><th>操作</th></tr></thead>
          <tbody>
            ${rows.map((row) => `
              <tr>
                <td>${escapeHtml(displayText(row.title, defaultWorkflowTitle(row)))}</td>
                <td>${escapeHtml(row.processId || row.workflow_id || "-")}</td>
                <td>${escapeHtml(displayAction(row.action))}</td>
                <td>${renderStatus(row.status || "-")}</td>
                <td>${escapeHtml(formatDate(row.time || row.handledAt))}</td>
                <td><button class="small-btn" type="button" data-detail="${escapeHtml(row.processId || row.workflow_id || row.workflowId)}">详情</button></td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      ` : renderEmpty("暂无审批记录")}
    </div>
  `;
}

async function renderUsers() {
  const [users, roles] = await Promise.all([api("/users"), api("/roles")]);
  const userRows = filterUserRows(Array.isArray(users.body.data) ? users.body.data : []);
  const roleRows = Array.isArray(roles.body.data) ? roles.body.data : [];
  els.content.innerHTML = `
    <div class="toolbar">
      <div class="filters">
        <input id="userKeyword" placeholder="搜索账号或姓名" value="${escapeHtml(state.userKeyword)}" />
        <select id="userRole">
          ${renderStatusOption("ALL", "全部角色", state.userRole)}
          ${renderStatusOption("USER", "普通用户", state.userRole)}
          ${renderStatusOption("STAFF", "工作人员", state.userRole)}
          ${renderStatusOption("ADMIN", "管理员", state.userRole)}
        </select>
        <button class="small-btn" type="button" onclick="renderUsers()">查询</button>
      </div>
      <button class="primary compact" type="button" data-user-action="create">新增用户</button>
    </div>
    <div class="data-panel">
      <h3>用户管理</h3>
      <table>
        <thead><tr><th>账号</th><th>姓名</th><th>角色</th><th>状态</th><th>村庄</th><th>操作</th></tr></thead>
        <tbody>
          ${userRows.map((row) => `
            <tr>
              <td>${escapeHtml(row.username)}</td>
              <td>${escapeHtml(displayText(row.displayName, row.username))}</td>
              <td>${renderStatus(row.role)}</td>
              <td>${renderStatus(row.enabled === false ? "inactive" : "active")}</td>
              <td>${escapeHtml(row.villageId || "-")}</td>
              <td>
                <div class="row-actions">
                  <button class="small-btn" type="button" data-user-action="role" data-user-id="${escapeHtml(row.id)}">调整角色</button>
                  <button class="small-btn" type="button" data-user-action="reset" data-user-id="${escapeHtml(row.id)}">重置密码</button>
                  ${row.enabled === false
                    ? `<button class="small-btn approve" type="button" data-user-action="enable" data-user-id="${escapeHtml(row.id)}">启用</button>`
                    : `<button class="small-btn reject" type="button" data-user-action="disable" data-user-id="${escapeHtml(row.id)}">停用</button>`}
                </div>
              </td>
            </tr>
          `).join("")}
        </tbody>
      </table>
    </div>
    <div class="data-panel">
      <h3>角色信息</h3>
      <table>
        <thead><tr><th>角色</th><th>名称</th></tr></thead>
        <tbody>
          ${roleRows.map((row) => `<tr><td>${escapeHtml(displayStatus(row.code))}</td><td>${escapeHtml(displayText(row.name, displayStatus(row.code)))}</td></tr>`).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderSystem() {
  els.content.innerHTML = `
    <div class="kpi-grid">
      ${renderKpi("接口地址", state.baseUrl)}
      ${renderKpi("当前账号", state.user.username)}
      ${renderKpi("当前角色", state.user.role)}
      ${renderKpi("权限数量", state.user.permissions?.length || 0)}
    </div>
    <div class="data-panel">
      <h3>基础权限</h3>
      <table>
        <thead><tr><th>权限码</th></tr></thead>
        <tbody>
          ${(state.user.permissions || []).map((item) => `<tr><td>${escapeHtml(item)}</td></tr>`).join("")}
        </tbody>
      </table>
    </div>
  `;
}

function renderForbidden() {
  els.content.innerHTML = `
    <div class="permission">
      <strong>当前账号无权进入 Web 工作台</strong>
      请联系管理员开通工作人员或管理员权限。
    </div>
  `;
}

async function handleApproval(workflowId, action) {
  if (!workflowId) return;
  const actionText = action === "approve" ? "通过" : "驳回";
  if (!window.confirm(`确认${actionText}该申请？`)) return;
  const path = action === "approve" ? `/workflows/${workflowId}/approve` : `/workflows/${workflowId}/reject`;
  try {
    await api(path, {
      method: "POST",
      body: { remark: action === "approve" ? "Web 工作台审批通过" : "Web 工作台驳回" },
    });
    await renderTodos();
  } catch (error) {
    window.alert(error.message || "审批失败，请刷新后重试");
    await renderTodos();
  }
}

async function openWorkflowDetail(workflowId) {
  if (!workflowId) return;
  els.detailDrawer.classList.remove("hidden");
  els.detailDrawer.setAttribute("aria-hidden", "false");
  els.detailTitle.textContent = "申请详情";
  els.detailContent.innerHTML = renderEmpty("加载中...");
  try {
    const result = await api(`/workflows/${workflowId}`);
    const detail = result.body.data || {};
    els.detailTitle.textContent = displayText(detail.title, "申请详情");
    els.detailContent.innerHTML = `
      <div class="detail-stack">
        <div class="detail-row"><span>申请编号</span><strong>${escapeHtml(detail.id || workflowId)}</strong></div>
        <div class="detail-row"><span>当前状态</span>${renderStatus(detail.status)}</div>
        <div class="detail-row"><span>申请人</span><strong>${escapeHtml(displayText(detail.applicantName, "-"))}</strong></div>
        <div class="detail-row"><span>当前节点</span><strong>${escapeHtml(displayText(detail.currentNodeId, "-"))}</strong></div>
      </div>
      <h4>审批记录</h4>
      ${(detail.records || []).length ? `
        <table>
          <thead><tr><th>节点</th><th>操作人</th><th>动作</th><th>意见</th></tr></thead>
          <tbody>
            ${detail.records.map((row) => `
              <tr>
                <td>${escapeHtml(displayText(row.nodeId, "-"))}</td>
                <td>${escapeHtml(displayText(row.operator, "-"))}</td>
                <td>${escapeHtml(displayAction(row.action))}</td>
                <td>${escapeHtml(displayText(row.remark, "无"))}</td>
              </tr>
            `).join("")}
          </tbody>
        </table>
      ` : renderEmpty("暂无审批记录")}
    `;
  } catch (error) {
    els.detailContent.innerHTML = `<div class="empty danger">${escapeHtml(error.message)}</div>`;
  }
}

function closeWorkflowDetail() {
  els.detailDrawer.classList.add("hidden");
  els.detailDrawer.setAttribute("aria-hidden", "true");
}

async function handleResourceAction(action, resourceId) {
  if (action === "create") {
    await createResource();
    return;
  }
  if (!resourceId) return;
  if (action === "detail") {
    await openResourceDetail(resourceId);
    return;
  }
  if (action === "edit") {
    await editResource(resourceId);
    return;
  }
  if (action === "publish") {
    if (!window.confirm("确认发布该资源并设为可招商？")) return;
    await api(`/resources/${resourceId}/publish`, { method: "POST" });
    await renderResources();
    return;
  }
  if (action === "offline") {
    if (!window.confirm("确认下架该资源？下架后普通用户不能提交合作申请。")) return;
    await api(`/resources/${resourceId}/offline`, { method: "POST" });
    await renderResources();
    return;
  }
  if (action === "status") {
    const status = window.prompt("请输入招商状态：可招商 / 洽谈中 / 已签约 / 已下架", "洽谈中");
    if (!status) return;
    await api(`/resources/${resourceId}/investment-status`, { method: "POST", body: { investmentStatus: status.trim() } });
    await renderResources();
  }
}

async function createResource() {
  const name = window.prompt("资源名称", "青耘共创空间");
  if (!name) return;
  const category = window.prompt("资源类型", "闲置农房") || "闲置农房";
  const address = window.prompt("资源地址", "青耘村") || "青耘村";
  await api("/resources", {
    method: "POST",
    body: { name: name.trim(), category: category.trim(), address: address.trim(), investmentStatus: "可招商" },
  });
  await renderResources();
}

async function editResource(resourceId) {
  const result = await api(`/resources/${resourceId}`);
  const resource = result.body.data || {};
  const name = window.prompt("资源名称", displayText(resource.name, ""));
  if (!name) return;
  const category = window.prompt("资源类型", displayText(resource.category, "闲置农房")) || resource.category;
  const address = window.prompt("资源地址", displayText(resource.address, "青耘村")) || resource.address;
  const investmentStatus = window.prompt("招商状态", displayText(resource.investmentStatus, "可招商")) || resource.investmentStatus;
  await api(`/resources/${resourceId}`, {
    method: "PUT",
    body: {
      name: name.trim(),
      category: String(category || "").trim(),
      address: String(address || "").trim(),
      investmentStatus: String(investmentStatus || "").trim(),
    },
  });
  await renderResources();
}

async function openResourceDetail(resourceId) {
  els.detailDrawer.classList.remove("hidden");
  els.detailDrawer.setAttribute("aria-hidden", "false");
  els.detailTitle.textContent = "资源详情";
  els.detailContent.innerHTML = renderEmpty("加载中...");
  try {
    const [detailResult, countResult] = await Promise.all([
      api(`/resources/${resourceId}`),
      api(`/resources/${resourceId}/applications/count`).catch(() => ({ body: { data: { applicationCount: 0 } } })),
    ]);
    const resource = detailResult.body.data || {};
    const count = countResult.body.data || {};
    els.detailTitle.textContent = displayText(resource.name, "资源详情");
    els.detailContent.innerHTML = `
      <div class="detail-stack">
        <div class="detail-row"><span>资源编号</span><strong>${escapeHtml(resource.id || resourceId)}</strong></div>
        <div class="detail-row"><span>资源类型</span><strong>${escapeHtml(displayCategory(resource.category))}</strong></div>
        <div class="detail-row"><span>招商状态</span>${renderStatus(resource.investmentStatus || "-")}</div>
        <div class="detail-row"><span>合作申请数</span><strong>${escapeHtml(count.applicationCount || 0)}</strong></div>
        <div class="detail-row"><span>面积</span><strong>${escapeHtml(resource.area || "-")}</strong></div>
        <div class="detail-row"><span>年收益预估</span><strong>${escapeHtml(resource.annualEstimate || "-")}</strong></div>
        <div class="detail-row"><span>联系人</span><strong>${escapeHtml(displayText(resource.owner || resource.contact, "-"))}</strong></div>
      </div>
      <h4>资源介绍</h4>
      <div class="empty left">${escapeHtml(displayText(resource.intro, "暂无介绍"))}</div>
    `;
  } catch (error) {
    els.detailContent.innerHTML = `<div class="empty danger">${escapeHtml(error.message)}</div>`;
  }
}

async function handleUserAction(action, userId) {
  if (action === "create") {
    await createUser();
    return;
  }
  if (!userId) return;
  if (action === "enable") {
    await api(`/users/${userId}/enable`, { method: "POST" });
  }
  if (action === "disable") {
    if (!window.confirm("确认停用该账号？")) return;
    await api(`/users/${userId}/disable`, { method: "POST" });
  }
  if (action === "reset") {
    if (!window.confirm("确认将密码重置为 123456？")) return;
    await api(`/users/${userId}/password`, { method: "POST", body: { password: "123456" } });
  }
  if (action === "role") {
    const role = window.prompt("请输入角色：USER / STAFF / ADMIN", "STAFF");
    if (!role) return;
    await api(`/users/${userId}/roles/${role.trim().toUpperCase()}`, { method: "POST" });
  }
  await renderUsers();
}

async function createUser() {
  const username = window.prompt("登录账号");
  if (!username) return;
  const displayName = window.prompt("姓名", username) || username;
  const role = (window.prompt("角色：USER / STAFF / ADMIN", "USER") || "USER").trim().toUpperCase();
  await api("/users", {
    method: "POST",
    body: { username: username.trim(), displayName: displayName.trim(), role, password: "123456" },
  });
  await renderUsers();
}

async function api(path, options = {}) {
  const headers = { "Content-Type": "application/json" };
  if (state.token && !options.skipAuth) {
    headers.Authorization = `Bearer ${state.token}`;
  }
  let response;
  try {
    response = await fetch(`${state.baseUrl}${path}`, {
      method: options.method || "GET",
      headers,
      body: options.body ? JSON.stringify(options.body) : undefined,
    });
  } catch (error) {
    throw new Error("无法连接服务器，请确认系统服务已正常启动");
  }
  let body = {};
  try {
    body = await response.json();
  } catch (error) {
    body = { code: response.status, message: response.statusText };
  }
  if (typeof body === "string") {
    try {
      body = JSON.parse(body);
    } catch (error) {
      body = { code: response.status, message: "响应数据格式错误" };
    }
  }
  if (!response.ok || body.code !== 200) {
    throw new Error(toFriendlyError(body.message || response.statusText || "请求失败"));
  }
  return { response, body };
}

function availableViews() {
  const role = state.user?.role;
  return NAV.filter((item) => item.roles.includes(role));
}

function filterRows(rows, keyword, status) {
  const text = (keyword || "").trim().toLowerCase();
  return rows.filter((row) => {
    const rowStatus = normalizeDisplayValue(row.status || row.action || "");
    const matchStatus = !status || status === "ALL" || rowStatus === status;
    const haystack = [
      row.id,
      row.processId,
      row.workflowId,
      row.workflow_id,
      row.title,
      row.category,
      row.applicant,
      row.status,
    ].map((item) => normalizeDisplayValue(item).toLowerCase()).join(" ");
    const matchKeyword = !text || haystack.includes(text);
    return matchStatus && matchKeyword;
  });
}

function filterUserRows(rows) {
  const text = state.userKeyword.trim().toLowerCase();
  return rows.filter((row) => {
    const matchRole = state.userRole === "ALL" || row.role === state.userRole;
    const haystack = [row.username, row.displayName, row.role, row.id]
      .map((item) => normalizeDisplayValue(item).toLowerCase())
      .join(" ");
    return matchRole && (!text || haystack.includes(text));
  });
}

function renderStatusOption(value, label, active) {
  return `<option value="${escapeHtml(value)}" ${value === active ? "selected" : ""}>${escapeHtml(label)}</option>`;
}

function renderKpi(title, value, index = 0) {
  const icons = ["仓", "链", "审", "险"];
  const deltas = ["12.5%", "8.7%", "33.3%", "50.0%"];
  const trendClass = index === 2 ? "warn" : "up";
  return `
    <div class="kpi kpi-${index + 1}">
      <div class="kpi-icon">${escapeHtml(icons[index] || "数")}</div>
      <div class="kpi-body">
        <span>${escapeHtml(title)}</span>
        <strong>${escapeHtml(value)}</strong>
        <em class="${trendClass}">较上周期 ${index === 3 ? "↓" : "↑"} ${deltas[index] || "6.8%"}</em>
      </div>
      <svg class="sparkline" viewBox="0 0 96 42" aria-hidden="true">
        <polyline points="${sparklinePoints(index)}" />
      </svg>
    </div>
  `;
}

function renderTrendTable(rows) {
  if (!rows.length) return renderEmpty("暂无趋势数据");
  return `
    <table>
      <thead><tr><th>日期</th><th>指标值</th></tr></thead>
      <tbody>
        ${rows.map((row) => `<tr><td>${escapeHtml(row.date)}</td><td>${escapeHtml(row.value)}</td></tr>`).join("")}
      </tbody>
    </table>
  `;
}

function renderTrendVisual(rows) {
  if (!rows.length) return renderEmpty("暂无趋势数据");
  const values = rows.map((row) => Number(row.value || 0));
  const max = Math.max(...values, 1);
  const min = Math.min(...values, 0);
  const width = 760;
  const height = 280;
  const left = 48;
  const right = 26;
  const top = 26;
  const bottom = 44;
  const plotWidth = width - left - right;
  const plotHeight = height - top - bottom;
  const points = rows.map((row, index) => {
    const x = left + (rows.length === 1 ? 0 : (index / (rows.length - 1)) * plotWidth);
    const y = top + (1 - ((Number(row.value || 0) - min) / Math.max(max - min, 1))) * plotHeight;
    return { x, y, row, value: Number(row.value || 0) };
  });
  const polyline = points.map((point) => `${point.x.toFixed(1)},${point.y.toFixed(1)}`).join(" ");
  const area = `${left},${height - bottom} ${polyline} ${width - right},${height - bottom}`;
  const labels = points.filter((_, index) => rows.length <= 10 || index % Math.ceil(rows.length / 8) === 0 || index === rows.length - 1);
  return `
    <div class="trend-visual svg-trend">
      <svg viewBox="0 0 ${width} ${height}" role="img" aria-label="运营趋势图">
        <defs>
          <linearGradient id="trendArea" x1="0" x2="0" y1="0" y2="1">
            <stop offset="0%" stop-color="#2f9b61" stop-opacity="0.28" />
            <stop offset="100%" stop-color="#2f9b61" stop-opacity="0.02" />
          </linearGradient>
        </defs>
        ${[0, 1, 2, 3].map((line) => {
          const y = top + (line / 3) * plotHeight;
          const value = Math.round(max - (line / 3) * (max - min));
          return `<g class="grid-line"><line x1="${left}" x2="${width - right}" y1="${y}" y2="${y}" /><text x="10" y="${y + 4}">${escapeHtml(value)}</text></g>`;
        }).join("")}
        <polygon class="trend-area" points="${area}" />
        <polyline class="trend-line primary-line" points="${polyline}" />
        <polyline class="trend-line secondary-line" points="${points.map((point) => `${point.x.toFixed(1)},${Math.min(height - bottom, point.y + 34).toFixed(1)}`).join(" ")}" />
        ${points.map((point) => `
          <g class="trend-point">
            <circle cx="${point.x}" cy="${point.y}" r="5" />
            <title>${escapeHtml(point.row.date)}：${escapeHtml(point.value)}</title>
          </g>
        `).join("")}
        ${labels.map((point) => `<text class="axis-label" x="${point.x}" y="${height - 12}">${escapeHtml(point.row.date)}</text>`).join("")}
      </svg>
    </div>
  `;
}

function renderCategoryDonut(stats) {
  const total = Number(stats[0]?.value || stats.length || 17) || 17;
  const items = [
    ["农业资源", Math.max(1, Math.round(total * 0.42)), "#2f9b61"],
    ["文旅空间", Math.max(1, Math.round(total * 0.24)), "#3b82f6"],
    ["研学基地", Math.max(1, Math.round(total * 0.18)), "#f59e0b"],
    ["集体资产", Math.max(1, Math.round(total * 0.12)), "#8b5cf6"],
  ];
  return `
    <div class="donut-wrap">
      <div class="donut-chart" style="--a:41%; --b:64%; --c:82%;">
        <strong>${escapeHtml(total)}</strong>
        <span>总数</span>
      </div>
      <div class="chart-legend">
        ${items.map(([label, value, color]) => `<p><i style="background:${color}"></i><span>${escapeHtml(label)}</span><strong>${escapeHtml(value)}</strong></p>`).join("")}
      </div>
    </div>
  `;
}

function renderApprovalBars(stats) {
  const todo = Number(stats[2]?.value || 8);
  const ready = Number(stats[1]?.value || 10);
  const risk = Number(stats[3]?.value || 1);
  const rows = [
    ["资源申请", Math.max(6, ready + 2), 78],
    ["合作申请", Math.max(4, todo + 1), 56],
    ["变更申请", Math.max(3, Math.round(todo * 0.75)), 43],
    ["其他流程", Math.max(2, risk + 4), 32],
  ];
  return `
    <div class="approval-bars">
      ${rows.map(([label, value, height]) => `
        <div class="approval-bar">
          <span>${escapeHtml(value)}</span>
          <i style="height:${height}%"></i>
          <em>${escapeHtml(label)}</em>
        </div>
      `).join("")}
    </div>
  `;
}

function renderHeatmap(rows) {
  const labels = ["资源访问", "合作咨询", "申请提交", "审批处理"];
  const values = rows.length ? rows : Array.from({ length: 7 }, (_, index) => ({ value: (index + 2) * 360, date: `07-0${index + 1}` }));
  const max = Math.max(...values.map((row) => Number(row.value || 0)), 1);
  return `
    <div class="heatmap">
      ${labels.map((label, rowIndex) => `
        <div class="heatmap-row">
          <span>${escapeHtml(label)}</span>
          ${values.slice(-7).map((item, index) => {
            const strength = Math.max(0.16, Math.min(1, (Number(item.value || 0) / max) * (0.62 + rowIndex * 0.12) + index * 0.015));
            return `<i style="--heat:${strength.toFixed(2)}" title="${escapeHtml(label)} ${escapeHtml(item.date)}"></i>`;
          }).join("")}
        </div>
      `).join("")}
      <div class="heatmap-axis">${values.slice(-7).map((item) => `<span>${escapeHtml(item.date)}</span>`).join("")}</div>
    </div>
  `;
}

function renderActivityTop(rows) {
  const names = ["溪畔共享民宿组团", "稻田研学课堂", "竹林露营营地", "老粮仓文创展厅", "农产品加工间"];
  const base = rows.slice(-5).map((row) => Number(row.value || 0));
  const max = Math.max(...base, 1);
  return `
    <div class="activity-list">
      ${names.map((name, index) => {
        const value = base[index] || Math.max(360, max - index * 420);
        return `
          <div class="activity-item">
            <b>${index + 1}</b>
            <span>${escapeHtml(name)}</span>
            <strong>${escapeHtml(value)}</strong>
            <i style="width:${Math.max(18, Math.round((value / max) * 100))}%"></i>
          </div>
        `;
      }).join("")}
    </div>
  `;
}

function sparklinePoints(index) {
  const sets = [
    "4,34 16,28 28,20 40,23 52,14 64,18 76,7 92,16",
    "4,28 16,22 28,26 40,18 52,20 64,10 76,23 92,8",
    "4,24 16,20 28,26 40,18 52,28 64,12 76,18 92,6",
    "4,18 16,30 28,16 40,28 52,12 64,34 76,18 92,10",
  ];
  return sets[index] || sets[0];
}

function renderApprovalActions(workflowId, status) {
  if (status !== "PENDING") return `<span class="muted">已处理</span>`;
  return `
    <div class="row-actions">
      <button class="small-btn approve" type="button" data-approval="approve" data-workflow-id="${escapeHtml(workflowId)}">通过</button>
      <button class="small-btn reject" type="button" data-approval="reject" data-workflow-id="${escapeHtml(workflowId)}">驳回</button>
    </div>
  `;
}

function renderStatus(value) {
  return `<span class="status">${escapeHtml(displayStatus(value))}</span>`;
}

function displayStatus(value) {
  const text = normalizeDisplayValue(value);
  const map = {
    PENDING: "待审批",
    APPROVED: "已通过",
    REJECTED: "已驳回",
    USER: "普通用户",
    STAFF: "工作人员",
    ADMIN: "管理员",
    COOPERATION_APPLICATION: "合作申请",
    active: "启用",
    inactive: "停用",
    published: "已发布",
    offline: "已下架",
    "可招商": "可招商",
    "洽谈中": "洽谈中",
    "已签约": "已签约",
    "已下架": "已下架",
    "待处理": "待处理",
    "进行中": "进行中",
    "已逾期": "已逾期",
    "已完成": "已完成",
  };
  return map[text] || displayText(text, "-");
}

function displayCategory(value) {
  const text = normalizeDisplayValue(value);
  const map = {
    COOPERATION_APPLICATION: "合作申请",
    "项目申报": "项目申报",
    "资产流转": "资产流转",
    "活动筹备": "活动筹备",
    "村民议事": "村民议事",
    "闲置农房": "闲置农房",
    "土地": "土地",
    "文旅空间": "文旅空间",
  };
  return map[text] || displayText(text, "未分类");
}

function displayAction(value) {
  const text = normalizeDisplayValue(value);
  const map = {
    APPROVED: "通过",
    REJECTED: "驳回",
    approve: "通过",
    reject: "驳回",
    pass: "通过",
  };
  return map[text] || displayText(text, "-");
}

function defaultWorkflowTitle(row = {}) {
  const category = row.category || row.categoryName;
  if (category === "COOPERATION_APPLICATION") return "合作申请";
  return "流程事项";
}

function displayText(value, fallback = "-") {
  const text = normalizeDisplayValue(value);
  if (!text || isGarbledText(text)) return fallback;
  return text;
}

function normalizeDisplayValue(value) {
  const text = String(value ?? "").trim();
  if (!text) return "";
  const repaired = repairUtf8Mojibake(text);
  return repaired || text;
}

function repairUtf8Mojibake(text) {
  if (!/[ÃÂÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ-]/.test(text)) {
    return text;
  }
  try {
    const encoded = Array.from(text)
      .map((char) => {
        const code = char.charCodeAt(0);
        return code <= 255 ? `%${code.toString(16).padStart(2, "0")}` : char;
      })
      .join("");
    const decoded = decodeURIComponent(encoded);
    return decoded && !/[ÃÂÄÅÆÇÈÉÊËÌÍÎÏÐÑÒÓÔÕÖ×ØÙÚÛÜÝÞßàáâãäåæçèéêëìíîïðñòóôõö÷øùúûüýþÿ-]/.test(decoded)
      ? decoded
      : text;
  } catch (error) {
    return text;
  }
}

function isGarbledText(text) {
  if (!text) return true;
  if (/�/.test(text)) return true;
  if (/\?{2,}/.test(text)) return true;
  if (/[鐢ㄦ埛瀵嗙爜璧勬簮寰呭姙瀹℃壒绯荤粺]/.test(text)) return true;
  if (/[èµå®æçä½]/.test(text)) return true;
  return false;
}

function renderEmpty(text) {
  return `<div class="empty">${escapeHtml(text)}</div>`;
}

function setLoading() {
  els.content.innerHTML = renderEmpty("加载中...");
}

function renderError(message) {
  els.content.innerHTML = `<div class="empty danger">${escapeHtml(message)}</div>`;
}

function setMessage(message, isError = false) {
  els.formMessage.textContent = message;
  els.formMessage.classList.toggle("visible", Boolean(message));
  els.formMessage.classList.toggle("danger", isError);
}

function setLoginButtonLoading(isLoading) {
  els.submitAuthBtn.disabled = isLoading;
  if (isLoading) {
    els.submitAuthBtn.textContent = state.authMode === "register" ? "正在注册..." : "正在登录...";
    return;
  }
  els.submitAuthBtn.textContent = state.authMode === "register" ? "注册并进入" : "登录";
}

function markLoginInvalid(isInvalid) {
  document.querySelectorAll(".login-form-panel .input-shell").forEach((item) => {
    item.classList.toggle("invalid", isInvalid);
  });
}

function togglePasswordVisibility() {
  const shouldShow = els.password.type === "password";
  els.password.type = shouldShow ? "text" : "password";
  els.passwordToggle.textContent = shouldShow ? "隐藏" : "显示";
  els.passwordToggle.setAttribute("aria-label", shouldShow ? "隐藏密码" : "显示密码");
}

function toggleAuthMode() {
  setAuthMode(state.authMode === "login" ? "register" : "login");
}

function setAuthMode(mode) {
  state.authMode = mode;
  const isRegister = mode === "register";
  els.displayNameField.classList.toggle("hidden", !isRegister);
  els.confirmPasswordField.classList.toggle("hidden", !isRegister);
  els.authTitle.textContent = isRegister ? "注册账号" : "登录工作台";
  els.authSubtitle.textContent = isRegister ? "注册后可在小程序端提交合作申请" : "使用工作人员或管理员账号进入";
  els.authSwitchText.textContent = isRegister ? "已有账号？" : "没有账号？";
  els.authModeToggle.textContent = isRegister ? "返回登录" : "注册普通用户";
  els.password.setAttribute("autocomplete", isRegister ? "new-password" : "current-password");
  els.confirmPassword.value = "";
  setMessage("");
  markLoginInvalid(false);
  setLoginButtonLoading(false);
}

function toFriendlyError(message = "", fallback = "系统服务暂时不可用，请稍后重试") {
  const text = String(message || "");
  if (/NetworkError|Load failed|无法连接|fetch/i.test(text)) {
    return "无法连接服务器，请确认系统服务已正常启动";
  }
  if (/已存在|duplicate|409/i.test(text)) {
    return "该账号已存在，请更换账号或直接登录";
  }
  if (/401|未登录|token|Token|登录状态|Unauthorized/i.test(text)) {
    return "登录状态已失效，请重新登录";
  }
  if (/密码|账号|credential|Bad credentials|用户名/i.test(text)) {
    return "账号或密码错误，请重新输入";
  }
  if (/无权|权限|forbidden|Forbidden|USER/i.test(text)) {
    return "当前账号无权进入管理工作台";
  }
  if (/500|503|服务|系统|Internal|Unavailable/i.test(text)) {
    return "系统服务暂时不可用，请稍后重试";
  }
  return text || fallback;
}

function saveSession() {
  localStorage.removeItem("xiangyun.baseUrl");
  localStorage.setItem("xiangyun.token", state.token);
  localStorage.setItem("xiangyun.user", JSON.stringify(state.user));
  localStorage.setItem("xiangyun.view", state.view);
}

function clearSession() {
  state.token = "";
  state.user = null;
  state.view = "dashboard";
  localStorage.removeItem("xiangyun.token");
  localStorage.removeItem("xiangyun.user");
  localStorage.removeItem("xiangyun.view");
}

function readJson(value) {
  if (!value) return null;
  try {
    return JSON.parse(value);
  } catch (error) {
    return null;
  }
}

function formatDate(value) {
  if (!value) return "-";
  return String(value).replace("T", " ").replace(".000+00:00", "");
}

function escapeHtml(value) {
  return String(value ?? "")
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;")
    .replace(/'/g, "&#039;");
}
