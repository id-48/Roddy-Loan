<?php

declare(strict_types=1);

require __DIR__ . '/bootstrap.php';

$loginError = '';
$page = (string)($_GET['page'] ?? 'dashboard');

if (admin() !== [] && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'logout') {
    try { requireCsrf(); } catch (Throwable) {}
    $_SESSION = [];
    session_destroy();
    header('Location: index.php', true, 303);
    exit;
}

if (admin() === [] && $_SERVER['REQUEST_METHOD'] === 'POST' && ($_POST['action'] ?? '') === 'login') {
    try {
        requireCsrf();
        $email = strtolower(post('email', 254));
        $password = post('password', 1024);
        $attempts = (int)($_SESSION['login_attempts'] ?? 0);
        $lockedUntil = (int)($_SESSION['login_locked_until'] ?? 0);
        if ($lockedUntil > time()) {
            throw new RuntimeException('locked');
        }
        $ok = hash_equals(ADMIN_EMAIL, $email) && password_verify($password, ADMIN_PASSWORD_HASH);
        if (!$ok) {
            password_verify($password, '$2y$10$92IXUNpkjO0rOQ5byMi.Ye4oKoEa3Ro9llC/.og/at2uheWG/igi.');
            $_SESSION['login_attempts'] = $attempts + 1;
            if ($attempts + 1 >= 5) {
                $_SESSION['login_locked_until'] = time() + 900;
                $_SESSION['login_attempts'] = 0;
            }
            throw new RuntimeException('bad');
        }
        session_regenerate_id(true);
        $_SESSION['admin'] = ['id' => 1, 'email' => ADMIN_EMAIL, 'name' => ADMIN_NAME, 'role' => ADMIN_ROLE];
        $_SESSION['last_activity'] = time();
        unset($_SESSION['csrf'], $_SESSION['login_attempts'], $_SESSION['login_locked_until']);
        redirect('dashboard');
    } catch (Throwable) {
        $loginError = 'Invalid email or password, or too many attempts. Please try again later.';
    }
}

if (admin() === []) {
    renderHeader('Admin sign in');
    ?>
    <section class="auth-card">
        <?php if ($loginError !== ''): ?><div class="flash error"><?= e($loginError) ?></div><?php endif; ?>
        <p>Secure access for authorized EMI Loan Calculator operations staff.</p>
        <form method="post" class="form-grid">
            <input type="hidden" name="csrf" value="<?= e(csrf()) ?>">
            <input type="hidden" name="action" value="login">
            <label>Email<input type="email" name="email" required autocomplete="username"></label>
            <label>Password<input type="password" name="password" required autocomplete="current-password"></label>
            <button class="button" type="submit">Sign in</button>
        </form>
    </section>
    <?php
    renderFooter();
    exit;
}

requireAdmin();
$settings = loadSettings();

/* ---- POST handlers ---- */
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = (string)($_POST['action'] ?? '');
    try {
        requireCsrf();
        if ($action === 'settings_save') {
            $base = post('ads_config_url', 500);
            if ($base === '') {
                $base = DEFAULT_API_BASE;
            }
            if (!str_ends_with($base, '/')) {
                $base .= '/';
            }
            $settings['ads_config_url'] = $base;
            $settings['versionCode'] = max(0, (int)post('versionCode', 20));
            $settings['applink'] = post('applink', 500);
            $settings['updateInprogress'] = isset($_POST['updateInprogress']);
            $settings['isSplashFullscreen'] = isset($_POST['isSplashFullscreen']);
            $settings['splashfullpriority'] = parsePriorityNumbers((string)($_POST['splashfullpriority'] ?? '0'), 0, 5);
            $settings['afterCallON'] = isset($_POST['afterCallON']);
            $settings['whatsappAfterCall'] = isset($_POST['whatsappAfterCall']);
            $orgPct = max(0, min(100, (int)($_POST['afterCallShowPercentOrganic'] ?? 100)));
            $mktPct = max(0, min(100, (int)($_POST['afterCallShowPercentMarketing'] ?? 100)));
            if (!isset($settings['organic']) || !is_array($settings['organic'])) {
                $settings['organic'] = emptyNetworkBundle();
            }
            if (!isset($settings['marketing']) || !is_array($settings['marketing'])) {
                $settings['marketing'] = emptyNetworkBundle();
            }
            $settings['organic']['afterCallShowPercent'] = $orgPct;
            $settings['marketing']['afterCallShowPercent'] = $mktPct;
            $settings['loanTrackerON'] = isset($_POST['loanTrackerON']);
            $settings['mainTest'] = false;
            saveSettings($settings);
            flash('', 'App settings saved.');
            redirect('settings');
        }
        if ($action === 'ads_save') {
            $active = (string)($_POST['active_audience'] ?? 'organic');
            if ($active !== 'marketing') {
                $active = 'organic';
            }
            // Only save the active tab — avoids hidden-panel defaults overwriting the other cohort
            $settings[$active] = readCohortFromPost($active);
            saveSettings($settings);
            flash('', 'Ads configuration saved for ' . ucfirst($active) . '.');
            redirect('ads_config', ['audience' => $active]);
        }
    } catch (Throwable $ex) {
        flash('error', $ex->getMessage());
        redirect($page === '' ? 'dashboard' : $page);
    }
}

if (!in_array($page, ['dashboard', 'settings', 'ads_config'], true)) {
    $page = 'dashboard';
}

/* ================= DASHBOARD ================= */
if ($page === 'dashboard') {
    renderHeader('Dashboard', 'dashboard');
    $summary = deviceSummary();
    $audienceFilter = (string)($_GET['audience'] ?? '');
    $p = max(1, (int)($_GET['p'] ?? 1));
    $list = devicePage($p, 20, $audienceFilter);
    ?>
    <section class="stat-grid">
        <article class="stat"><span>Total users</span><strong><?= e(number_format($summary['total'])) ?></strong></article>
        <article class="stat"><span>Organic</span><strong><?= e(number_format($summary['organic'])) ?></strong></article>
        <article class="stat"><span>Marketing</span><strong><?= e(number_format($summary['marketing'])) ?></strong></article>
    </section>
    <section class="panel">
        <div class="panel-title"><h2>App opens</h2></div>
        <p class="muted">Last open shown in <strong>IST</strong>, 12-hour AM/PM. MySQL DB: <code>u387533797_blastmohanloan</code> (table <code>app_open_devices</code>). Credentials in <code>admin/db_config.php</code>.</p>
        <form method="get" class="filters">
            <input type="hidden" name="page" value="dashboard">
            <select name="audience">
                <option value="">All audiences</option>
                <option value="organic" <?= $audienceFilter === 'organic' ? 'selected' : '' ?>>Organic</option>
                <option value="marketing" <?= $audienceFilter === 'marketing' ? 'selected' : '' ?>>Marketing</option>
            </select>
            <button class="button">Filter</button>
        </form>
        <?php if ($list['items'] === []): ?>
            <p class="empty">No app open data yet.</p>
        <?php else: ?>
            <div class="table-wrap">
                <table>
                    <thead><tr><th>No.</th><th>Device ID</th><th>Audience</th><th>Last Open</th></tr></thead>
                    <tbody>
                    <?php foreach ($list['items'] as $i => $row):
                        $no = (($list['page'] - 1) * $list['perPage']) + $i + 1;
                        $aud = (string)($row['audience'] ?? 'organic');
                        ?>
                        <tr>
                            <td><?= e((string)$no) ?></td>
                            <td class="device-id-cell"><?= e((string)$row['device_id']) ?></td>
                            <td><span class="pill <?= $aud === 'marketing' ? 'pill-purple' : 'pill-green' ?>"><?= e(ucfirst($aud)) ?></span></td>
                            <td class="notify-time"><?= e(formatIst((string)$row['last_opened_at'])) ?></td>
                        </tr>
                    <?php endforeach; ?>
                    </tbody>
                </table>
            </div>
            <nav class="pager">
                <?php if ($list['page'] > 1): ?>
                    <a class="button ghost" href="?page=dashboard&audience=<?= e($audienceFilter) ?>&p=<?= $list['page'] - 1 ?>">Previous</a>
                <?php endif; ?>
                <span>Page <?= e((string)$list['page']) ?> of <?= e((string)$list['pages']) ?></span>
                <?php if ($list['page'] < $list['pages']): ?>
                    <a class="button ghost" href="?page=dashboard&audience=<?= e($audienceFilter) ?>&p=<?= $list['page'] + 1 ?>">Next</a>
                <?php endif; ?>
            </nav>
        <?php endif; ?>
    </section>
    <?php
    renderFooter();
    exit;
}

/* ================= SETTINGS ================= */
if ($page === 'settings') {
    renderHeader('App settings', 'settings');
    ?>
    <form method="post">
        <input type="hidden" name="csrf" value="<?= e(csrf()) ?>">
        <input type="hidden" name="action" value="settings_save">

        <section class="panel">
            <h2>API base</h2>
            <p class="field-hint">Remote config URL — API base. App appends <code>ads_config.php</code>.</p>
            <label class="config-field">
                <span><strong>Remote config URL</strong></span>
                <input name="ads_config_url" value="<?= e((string)$settings['ads_config_url']) ?>">
            </label>
        </section>

        <section class="panel">
            <h2>App Update</h2>
            <p class="field-hint">Version check runs only when updateInprogress is ON. OFF = no dialog, app works normally. ON + versionCode mismatch = forced update dialog on splash.</p>
            <div class="grid-2">
                <label><span>versionCode</span><input type="number" name="versionCode" min="0" value="<?= e((string)(int)$settings['versionCode']) ?>"></label>
                <label><span>applink</span><input name="applink" placeholder="Play Store URL" value="<?= e((string)$settings['applink']) ?>"></label>
                <div class="switch-wrap"><label>updateInprogress (forced update)</label><label class="switch"><input type="checkbox" name="updateInprogress" <?= !empty($settings['updateInprogress']) ? 'checked' : '' ?>><i></i></label></div>
            </div>
        </section>

        <section class="panel">
            <h2>Splash Fullscreen Ad</h2>
            <p class="field-hint"><strong>isSplashFullscreen</strong> ON = show fullscreen after Splash. OFF = skip (no splash ad).</p>
            <p class="field-hint">Priority values: <strong>0</strong> = skip slot, <strong>1</strong> = interstitial, <strong>2</strong> = reward, <strong>3</strong> = app open, <strong>4</strong> = rewarded interstitial, <strong>5</strong> = custom link. Example: <code>1</code> or rotate <code>1,3</code>.</p>
            <div class="grid-2">
                <div class="switch-wrap"><label>isSplashFullscreen</label><label class="switch"><input type="checkbox" name="isSplashFullscreen" <?= !empty($settings['isSplashFullscreen']) ? 'checked' : '' ?>><i></i></label></div>
                <label><span>splashfullpriority</span><input name="splashfullpriority" value="<?= e(joinInts($settings['splashfullpriority'] ?? [1])) ?>"></label>
            </div>
        </section>

        <section class="panel">
            <h2>After Call</h2>
            <p class="field-hint">ON = show Call Ended Quick Actions dialog (note, call back, reminder, WhatsApp, message + native ad) when a phone call ends. OFF = hide.</p>
            <p class="field-hint">WhatsApp: ON = same dialog after WhatsApp voice/video call ends (needs Notification access on device). OFF = ignore WhatsApp calls.</p>
            <div class="grid-2">
                <div class="switch-wrap"><label>afterCallON</label><label class="switch"><input type="checkbox" name="afterCallON" <?= !empty($settings['afterCallON']) ? 'checked' : '' ?>><i></i></label></div>
                <div class="switch-wrap"><label>whatsappAfterCall</label><label class="switch"><input type="checkbox" name="whatsappAfterCall" <?= !empty($settings['whatsappAfterCall']) ? 'checked' : '' ?>><i></i></label></div>
                <p class="field-hint wide">WhatsApp needs <strong>Notification access</strong> on the device (Settings → Apps → Special access → Notification access).</p>
            </div>
            <?php
            $orgShowPct = (int)(($settings['organic']['afterCallShowPercent'] ?? 100));
            $mktShowPct = (int)(($settings['marketing']['afterCallShowPercent'] ?? 100));
            $orgShowPct = max(0, min(100, $orgShowPct));
            $mktShowPct = max(0, min(100, $mktShowPct));
            ?>
            <h3 style="margin-top:1.25rem">Show frequency % (phone + WhatsApp mixed)</h3>
            <p class="field-hint"><strong>100</strong> = every call end · <strong>50</strong> = every 2nd · <strong>25</strong> = every 4th. One shared counter for phone + WhatsApp. Organic / Marketing separate.</p>
            <div class="grid-2">
                <label><span>Organic afterCallShowPercent (0–100)</span>
                    <input type="number" name="afterCallShowPercentOrganic" min="0" max="100"
                           value="<?= e((string)$orgShowPct) ?>"></label>
                <label><span>Marketing afterCallShowPercent (0–100)</span>
                    <input type="number" name="afterCallShowPercentMarketing" min="0" max="100"
                           value="<?= e((string)$mktShowPct) ?>"></label>
            </div>
        </section>

        <section class="panel">
            <h2>Loan Tracker</h2>
            <p class="field-hint"><strong>loanTrackerON</strong> applies to <strong>marketing</strong> users only. Organic users never see the Loan Tracker card.</p>
            <p class="field-hint">Timeline after submit: day 0–1 Application Received → day 2 Processing → day 3 Complete → day 5 Loan Rejected (not eligible). Each step advance sends a local notification.</p>
            <div class="grid-2">
                <div class="switch-wrap"><label>loanTrackerON</label><label class="switch"><input type="checkbox" name="loanTrackerON" <?= !empty($settings['loanTrackerON']) ? 'checked' : '' ?>><i></i></label></div>
            </div>
        </section>

        <section class="panel">
            <h2>Onboarding Screen Order</h2>
            <p class="field-hint">Ab <strong>Ads configuration → Organic / Marketing</strong> tabs mein alag-alag set hota hai (is audience ke flow screens).</p>
        </section>

        <div class="config-actions"><button class="button" type="submit">Save settings</button></div>
    </form>
    <?php
    renderFooter();
    exit;
}

/* ================= ADS CONFIG ================= */
if ($page === 'ads_config') {
    $audience = (string)($_GET['audience'] ?? 'organic');
    if ($audience !== 'marketing') {
        $audience = 'organic';
    }
    renderHeader('Ads configuration', 'ads_config');
    ?>
    <div class="segment-tabs">
        <a class="segment-tab <?= $audience === 'organic' ? 'active' : '' ?>" href="?page=ads_config&audience=organic">Organic</a>
        <a class="segment-tab <?= $audience === 'marketing' ? 'active' : '' ?>" href="?page=ads_config&audience=marketing">Marketing</a>
    </div>
    <p class="muted">Different ad IDs / priorities per audience. Active tab (Organic or Marketing) is saved when you click Save.</p>
    <form method="post" id="adsForm">
        <input type="hidden" name="csrf" value="<?= e(csrf()) ?>">
        <input type="hidden" name="action" value="ads_save">
        <input type="hidden" name="active_audience" value="<?= e($audience) ?>">

        <div class="cohort-panel" data-cohort="organic" <?= $audience === 'organic' ? '' : 'hidden' ?>>
            <?php $prefix = 'organic'; $bundle = $settings['organic']; require __DIR__ . '/partials/ads_cohort_form.php'; ?>
        </div>
        <div class="cohort-panel" data-cohort="marketing" <?= $audience === 'marketing' ? '' : 'hidden' ?>>
            <?php $prefix = 'marketing'; $bundle = $settings['marketing']; require __DIR__ . '/partials/ads_cohort_form.php'; ?>
        </div>

        <!-- Keep inactive cohort fields in DOM so both save -->
        <div class="config-actions"><button class="button" type="submit">Save all changes</button></div>
    </form>
    <template id="adxRowTpl">
        <div class="config-grid adx-row" style="margin-top:8px">
            <label class="config-field"><span><strong>Ad ID</strong></span><input name="__P___adx___F___id[]" value=""></label>
            <label class="config-field"><span><strong>%</strong></span><input type="number" min="0" max="100" name="__P___adx___F___percent[]" value="0"></label>
            <div style="display:flex;align-items:end"><button type="button" class="button danger ghost adx-remove">Remove</button></div>
        </div>
    </template>
    <script>
    document.querySelectorAll('.adx-add').forEach(function (btn) {
        btn.addEventListener('click', function () {
            var p = btn.getAttribute('data-prefix');
            var f = btn.getAttribute('data-format');
            var host = document.querySelector('.adx-rows[data-prefix="'+p+'"][data-format="'+f+'"]');
            var tpl = document.getElementById('adxRowTpl');
            if (!host || !tpl) return;
            var html = tpl.innerHTML.split('__P__').join(p).split('__F__').join(f);
            var w = document.createElement('div'); w.innerHTML = html.trim();
            host.appendChild(w.firstElementChild);
        });
    });
    document.addEventListener('click', function (e) {
        if (e.target && e.target.classList.contains('adx-remove')) {
            var row = e.target.closest('.adx-row');
            if (!row) return;
            var parent = row.parentElement;
            if (parent && parent.querySelectorAll('.adx-row').length > 1) row.remove();
        }
    });
    </script>
    <?php
    renderFooter();
    exit;
}
