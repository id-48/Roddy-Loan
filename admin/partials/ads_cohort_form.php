<?php
/** @var string $prefix organic|marketing */
/** @var array $bundle */
$prefix = $prefix ?? 'organic';
$bundle = $bundle ?? emptyNetworkBundle();
?>
<div class="config-sections ads-config-sections">
<section class="panel config-card">
    <h2>Onboarding Screen Order</h2>
    <p class="muted">Comma-separated screen IDs (max 23). Jo IDs doge wahi screens dikhengi, isi order mein. Example: <code>1,2,3,4,5</code> or full <code>1,2,3,...,23</code></p>
    <p class="muted" style="font-size:12px;line-height:1.5">
        1=Language · 2=Currency · 3=Welcome · 4=Bank · 5=Loan type · 6=Purpose · 7=Amount · 8=Tenure · 9=Interest · 10=EMI preview ·
        11=Basic details · 12=Contact · 13=Address · 14=Employment · 15=Income · 16=Expense · 17=Spin · 18=AI analysis ·
        19=Final processing · 20=Risk · 21=Final review · 22=Approved · 23=Loan offer
    </p>
    <label class="config-field">
        <span><strong>onboardingScreenOrder</strong> <small>(<?= e(ucfirst($prefix)) ?> only)</small></span>
        <input name="<?= e($prefix) ?>_onboardingScreenOrder"
               value="<?= e(joinInts($bundle['onboardingScreenOrder'] ?? defaultOnboardingOrder())) ?>"
               placeholder="1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16,17,18,19,20,21,22,23">
    </label>
</section>

<section class="panel config-card">
    <h2>General</h2>
    <div class="config-grid">
        <label class="config-field config-field-switch">
            <span><strong>Ads enabled</strong><small>Master switch for this audience.</small></span>
            <span class="switch"><input type="checkbox" name="<?= e($prefix) ?>_ads_enabled" value="1" <?= !empty($bundle['ads_enabled']) ? 'checked' : '' ?>><i></i></span>
        </label>
        <label class="config-field config-field-switch">
            <span><strong>Splash ad (legacy)</strong><small>Kept for compatibility; prefer isSplashFullscreen in App settings.</small></span>
            <span class="switch"><input type="checkbox" name="<?= e($prefix) ?>_splash_ad_enabled" value="1" <?= !empty($bundle['splash_ad_enabled']) ? 'checked' : '' ?>><i></i></span>
        </label>
    </div>
</section>

<section class="panel config-card">
    <h2>Launcher (Home screen)</h2>
    <p class="muted"><strong>launcherON</strong> = set as default home + show app list launcher. OFF = normal app only.</p>
    <p class="muted"><strong>launcher_interval</strong> = launcher app-icon clicks pe fullscreen ad (0 = off). Example: <code>2</code> → har 2nd app open pe ad.</p>
    <div class="config-grid">
        <label class="config-field config-field-switch">
            <span><strong>launcherON</strong><small><?= e(ucfirst($prefix)) ?> users.</small></span>
            <span class="switch"><input type="checkbox" name="<?= e($prefix) ?>_launcherON" value="1" <?= !empty($bundle['launcherON']) ? 'checked' : '' ?>><i></i></span>
        </label>
        <label class="config-field">
            <span><strong>launcher_interval</strong></span>
            <input type="number" min="0" max="50" name="<?= e($prefix) ?>_launcher_interval"
                   value="<?= e((string)(int)($bundle['launcher_interval'] ?? 1)) ?>">
        </label>
    </div>
</section>

<section class="panel config-card">
    <h2>Click fullscreen ads</h2>
    <p class="muted"><strong>click_interval</strong> = har utne clicks pe ek fullscreen ad (0 = off). Example: <code>3</code> → har 3rd click pe ad.</p>
    <p class="muted"><strong>Full screen priority</strong>: <strong>1</strong>=Interstitial · <strong>2</strong>=Reward · <strong>3</strong>=App open · <strong>4</strong>=Custom. Example: <code>1,3,4</code> → pehli ad Interstitial, agli App open, agli Custom, phir rotate.</p>
    <div class="config-grid">
        <label class="config-field">
            <span><strong>click_interval</strong></span>
            <input type="number" min="0" max="50" name="<?= e($prefix) ?>_click_interval"
                   value="<?= e((string)(int)($bundle['click_interval'] ?? 1)) ?>">
        </label>
        <label class="config-field">
            <span><strong>Full screen priority</strong></span>
            <input name="<?= e($prefix) ?>_fullscreen_priority"
                   value="<?= e(joinInts($bundle['fullscreen_priority'] ?? [1, 2, 3, 4])) ?>"
                   placeholder="1,3,4">
        </label>
    </div>
</section>

<section class="panel config-card">
    <h2>Network priority</h2>
    <p class="muted">1 = AdMob · 2 = AdX · 3 = TradPlus · 4 = Custom. <strong>Rotate</strong> per ad: <code>1,2</code> → 1st ad AdMob, 2nd ad AdX, phir repeat. Same show pe fail ho to baaki priority try; <em>failed</em> list uske baad fallback hai.</p>
    <div class="config-grid">
        <?php foreach (['interstitial', 'rewarded', 'app_open', 'native'] as $fmt): ?>
        <label class="config-field">
            <span><strong><?= e(ucfirst(str_replace('_', ' ', $fmt))) ?> priority</strong></span>
            <input name="<?= e($prefix) ?>_priority_<?= e($fmt) ?>" value="<?= e(joinInts($bundle['priority'][$fmt] ?? [])) ?>" placeholder="1,2,3,4">
        </label>
        <?php endforeach; ?>
        <?php foreach (['interstitial', 'rewarded', 'app_open'] as $fmt): ?>
        <label class="config-field">
            <span><strong><?= e(ucfirst(str_replace('_', ' ', $fmt))) ?> failed</strong></span>
            <input name="<?= e($prefix) ?>_fallback_<?= e($fmt) ?>" value="<?= e(joinInts($bundle['fallback'][$fmt] ?? [])) ?>" placeholder="2,1">
        </label>
        <?php endforeach; ?>
    </div>
</section>

<section class="panel config-card">
    <h2>Facebook</h2>
    <p class="muted">App ID + Client Token (secret) for this audience only. Empty = skip Facebook SDK init for <?= e(ucfirst($prefix)) ?> users.</p>
    <div class="config-grid">
        <label class="config-field">
            <span><strong>Facebook App ID</strong></span>
            <input name="<?= e($prefix) ?>_fb_app_id"
                   value="<?= e((string)($bundle['fb_app_id'] ?? '')) ?>"
                   placeholder="1597936781846363">
        </label>
        <label class="config-field">
            <span><strong>Facebook Client Token</strong></span>
            <input name="<?= e($prefix) ?>_fb_client_token"
                   value="<?= e((string)($bundle['fb_client_token'] ?? '')) ?>"
                   placeholder="16da80328ac59d761e78a8961623f37f">
        </label>
    </div>
</section>

<section class="panel config-card">
    <h2>AdMob</h2>
    <div class="config-grid">
        <?php foreach ([
            'app_open_id' => 'App open',
            'interstitial_id' => 'Interstitial',
            'rewarded_id' => 'Rewarded',
            'rewarded_interstitial_id' => 'Rewarded interstitial',
            'native_id' => 'Native',
            'banner_id' => 'Banner',
        ] as $field => $label): ?>
        <label class="config-field">
            <span><strong><?= e($label) ?></strong></span>
            <input name="<?= e($prefix) ?>_admob_<?= e($field) ?>" value="<?= e((string)($bundle['networks']['admob'][$field] ?? '')) ?>">
        </label>
        <?php endforeach; ?>
    </div>
</section>

<section class="panel config-card">
    <h2>AdX</h2>
    <p class="muted">Multiple IDs with % — app picks by weighted random.</p>
    <?php foreach (['app_open' => 'App open', 'interstitial' => 'Interstitial', 'rewarded' => 'Rewarded', 'rewarded_interstitial' => 'Rewarded interstitial', 'native' => 'Native', 'banner' => 'Banner'] as $fmt => $label):
        $units = $bundle['networks']['adx'][$fmt] ?? [];
        if (!is_array($units) || $units === []) {
            $units = [['id' => '', 'percent' => 100]];
        }
    ?>
    <div class="adx-format" style="margin-top:14px;padding-top:14px;border-top:1px solid var(--line)">
        <div class="panel-title"><h2 style="font-size:15px;margin:0"><?= e($label) ?></h2>
            <button type="button" class="button ghost adx-add" data-prefix="<?= e($prefix) ?>" data-format="<?= e($fmt) ?>">Add ID</button>
        </div>
        <div class="adx-rows" data-prefix="<?= e($prefix) ?>" data-format="<?= e($fmt) ?>">
            <?php foreach ($units as $unit): ?>
            <div class="config-grid adx-row" style="margin-top:8px">
                <label class="config-field"><span><strong>Ad ID</strong></span>
                    <input name="<?= e($prefix) ?>_adx_<?= e($fmt) ?>_id[]" value="<?= e((string)($unit['id'] ?? '')) ?>">
                </label>
                <label class="config-field"><span><strong>%</strong></span>
                    <input type="number" min="0" max="100" name="<?= e($prefix) ?>_adx_<?= e($fmt) ?>_percent[]" value="<?= e((string)((int)($unit['percent'] ?? 0))) ?>">
                </label>
                <div style="display:flex;align-items:end"><button type="button" class="button danger ghost adx-remove">Remove</button></div>
            </div>
            <?php endforeach; ?>
        </div>
    </div>
    <?php endforeach; ?>
</section>

<section class="panel config-card">
    <h2>TradPlus</h2>
    <div class="config-grid">
        <?php foreach (['app_id' => 'App ID', 'app_open_id' => 'App open', 'interstitial_id' => 'Interstitial', 'rewarded_id' => 'Rewarded', 'native_id' => 'Native'] as $field => $label): ?>
        <label class="config-field">
            <span><strong><?= e($label) ?></strong></span>
            <input name="<?= e($prefix) ?>_tradplus_<?= e($field) ?>" value="<?= e((string)($bundle['networks']['tradplus'][$field] ?? '')) ?>">
        </label>
        <?php endforeach; ?>
    </div>
</section>

<section class="panel config-card">
    <h2>Custom</h2>
    <div class="config-grid">
        <label class="config-field wide">
            <span><strong>URL</strong><small>Single URL for Custom network (Chrome Custom Tabs).</small></span>
            <input name="<?= e($prefix) ?>_custom_custom_url" value="<?= e((string)($bundle['networks']['custom']['custom_url'] ?? '')) ?>" placeholder="https://example.com">
        </label>
    </div>
</section>
</div>
