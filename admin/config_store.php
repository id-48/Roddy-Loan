<?php

declare(strict_types=1);

const CONFIG_FILE = __DIR__ . '/data/app_settings.json';
const ASSET_FILE = __DIR__ . '/../app/src/main/assets/ads_config.json';
const DEFAULT_API_BASE = 'https://blastmohanloan.anthemknight.tech/';

function defaultOnboardingOrder(): array
{
    return range(1, 23);
}

function emptyNetworkBundle(): array
{
    return [
        'ads_enabled' => true,
        'splash_ad_enabled' => true,
        'test_mode' => true,
        'click_interval' => 1,
        'fullscreen_priority' => [1, 2, 3, 4],
        /** 100 = every call end, 50 = every 2nd, 25 = every 4th (phone + WhatsApp shared). */
        'afterCallShowPercent' => 100,
        'launcherON' => false,
        'launcher_interval' => 1,
        'fb_app_id' => '',
        'fb_client_token' => '',
        'onboardingScreenOrder' => defaultOnboardingOrder(),
        'priority' => [
            'interstitial' => [1, 2, 3, 4],
            'rewarded' => [1, 2, 3, 4],
            'app_open' => [1, 2, 3, 4],
            'native' => [1, 2, 3, 4],
        ],
        'fallback' => [
            'interstitial' => [2, 1],
            'rewarded' => [3, 2],
            'app_open' => [1, 2, 3],
        ],
        'networks' => [
            'admob' => [
                'app_open_id' => '',
                'interstitial_id' => '',
                'rewarded_id' => '',
                'rewarded_interstitial_id' => '',
                'native_id' => '',
                'banner_id' => '',
            ],
            'adx' => [
                'app_open' => [],
                'interstitial' => [],
                'rewarded' => [],
                'rewarded_interstitial' => [],
                'native' => [],
                'banner' => [],
            ],
            'tradplus' => [
                'app_id' => '',
                'app_open_id' => '',
                'interstitial_id' => '',
                'rewarded_id' => '',
                'native_id' => '',
            ],
            'custom' => [
                'custom_url' => '',
            ],
        ],
    ];
}

function defaultAppSettings(): array
{
    return [
        'ads_config_url' => DEFAULT_API_BASE,
        'versionCode' => 1,
        'applink' => 'https://play.google.com/store/apps/details?id=com.loan.emi.calculator.loanemicalc.finance.bankingcalculator.financialcalculator',
        'updateInprogress' => false,
        'isSplashFullscreen' => true,
        'splashfullpriority' => [1],
        'afterCallON' => false,
        'whatsappAfterCall' => false,
        'loanTrackerON' => false,
        'mainTest' => true,
        'organic' => emptyNetworkBundle(),
        'marketing' => emptyNetworkBundle(),
    ];
}

/**
 * Deep-merge settings. Sequential/list arrays (priority, fallback, etc.) are
 * replaced wholesale — array_replace_recursive would keep leftover defaults
 * (e.g. saved [2,1] + default [1,2,3] → [2,1,3]).
 */
function isListArray(array $arr): bool
{
    if ($arr === []) {
        return true;
    }
    return array_keys($arr) === range(0, count($arr) - 1);
}

function mergeSettings(array $base, array $overlay): array
{
    foreach ($overlay as $key => $value) {
        if (!array_key_exists($key, $base)) {
            $base[$key] = $value;
            continue;
        }
        if (is_array($value) && is_array($base[$key]) && !isListArray($value) && !isListArray($base[$key])) {
            $base[$key] = mergeSettings($base[$key], $value);
        } else {
            $base[$key] = $value;
        }
    }
    return $base;
}

function loadSettings(): array
{
    $base = defaultAppSettings();
    if (is_file(CONFIG_FILE)) {
        $decoded = json_decode((string)file_get_contents(CONFIG_FILE), true);
        if (is_array($decoded)) {
            $base = mergeSettings($base, $decoded);
            // migrate legacy key
            if (array_key_exists('afterCall', $decoded) && !array_key_exists('afterCallON', $decoded)) {
                $base['afterCallON'] = !empty($decoded['afterCall']);
            }
            unset($base['afterCall']);
            // Migrate root onboardingScreenOrder → both cohorts if missing
            if (!empty($decoded['onboardingScreenOrder']) && is_array($decoded['onboardingScreenOrder'])) {
                foreach (['organic', 'marketing'] as $cohort) {
                    if (empty($base[$cohort]['onboardingScreenOrder'])) {
                        $base[$cohort]['onboardingScreenOrder'] = array_values($decoded['onboardingScreenOrder']);
                    }
                }
            }
            unset($base['onboardingScreenOrder']);
            foreach (['organic', 'marketing'] as $cohort) {
                if (empty($base[$cohort]['onboardingScreenOrder']) || !is_array($base[$cohort]['onboardingScreenOrder'])) {
                    $base[$cohort]['onboardingScreenOrder'] = defaultOnboardingOrder();
                }
            }
        }
    } elseif (is_file(__DIR__ . '/ads_config.json')) {
        // migrate old flat ads_config into both cohorts
        $old = json_decode((string)file_get_contents(__DIR__ . '/ads_config.json'), true);
        if (is_array($old)) {
            if (!empty($old['ads_config_url'])) {
                $base['ads_config_url'] = $old['ads_config_url'];
            }
            foreach (['organic', 'marketing'] as $cohort) {
                foreach (['ads_enabled', 'splash_ad_enabled', 'priority', 'fallback', 'networks'] as $key) {
                    if (isset($old[$key])) {
                        $base[$cohort][$key] = $old[$key];
                    }
                }
            }
        }
    }
    return $base;
}

function saveSettings(array $settings): void
{
    $dir = dirname(CONFIG_FILE);
    if (!is_dir($dir)) {
        mkdir($dir, 0775, true);
    }
    $json = json_encode($settings, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES);
    if ($json === false) {
        throw new RuntimeException('Could not encode settings.');
    }
    file_put_contents(CONFIG_FILE, $json . "\n");
    // App asset mirror (organic + marketing root for client parse)
    $asset = [
        'ads_config_url' => $settings['ads_config_url'] ?? DEFAULT_API_BASE,
        'versionCode' => (int)($settings['versionCode'] ?? 1),
        'applink' => (string)($settings['applink'] ?? ''),
        'updateInprogress' => !empty($settings['updateInprogress']),
        'isSplashFullscreen' => !empty($settings['isSplashFullscreen']),
        'splashfullpriority' => array_values($settings['splashfullpriority'] ?? [1]),
        'afterCallON' => !empty($settings['afterCallON']),
        'whatsappAfterCall' => !empty($settings['whatsappAfterCall']),
        'loanTrackerON' => !empty($settings['loanTrackerON']),
        'mainTest' => !empty($settings['mainTest']),
        'organic' => $settings['organic'] ?? emptyNetworkBundle(),
        'marketing' => $settings['marketing'] ?? emptyNetworkBundle(),
    ];
    $assetDir = dirname(ASSET_FILE);
    if (is_dir($assetDir)) {
        file_put_contents(ASSET_FILE, json_encode($asset, JSON_PRETTY_PRINT | JSON_UNESCAPED_SLASHES) . "\n");
    }
}

function parsePriorityNumbers(string $raw, int $min = 0, int $max = 5): array
{
    $parts = array_filter(array_map('trim', explode(',', $raw)), static fn($v) => $v !== '');
    $out = [];
    foreach ($parts as $part) {
        if (!ctype_digit($part)) {
            throw new RuntimeException('Use numbers only, e.g. 1,2,3');
        }
        $n = (int)$part;
        if ($n < $min || $n > $max) {
            throw new RuntimeException("Numbers must be between {$min} and {$max}.");
        }
        $out[] = $n;
    }
    return $out;
}

function joinInts(array $list): string
{
    return implode(',', array_map('strval', $list));
}

function parseAdxUnits(string $prefix, string $format): array
{
    $ids = $_POST[$prefix . '_adx_' . $format . '_id'] ?? [];
    $percents = $_POST[$prefix . '_adx_' . $format . '_percent'] ?? [];
    if (!is_array($ids)) {
        $ids = [];
    }
    if (!is_array($percents)) {
        $percents = [];
    }
    $units = [];
    $count = max(count($ids), count($percents));
    for ($i = 0; $i < $count; $i++) {
        $id = trim((string)($ids[$i] ?? ''));
        if ($id === '') {
            continue;
        }
        $percent = max(0, min(100, (int)($percents[$i] ?? 0)));
        $units[] = ['id' => $id, 'percent' => $percent];
    }
    return $units;
}

function readCohortFromPost(string $prefix): array
{
    $bundle = emptyNetworkBundle();
    // Keep App-settings show % (not edited on Ads Config tab)
    $existing = loadSettings();
    if (isset($existing[$prefix]['afterCallShowPercent'])) {
        $bundle['afterCallShowPercent'] = max(0, min(100, (int)$existing[$prefix]['afterCallShowPercent']));
    }
    $bundle['ads_enabled'] = isset($_POST[$prefix . '_ads_enabled']);
    $bundle['splash_ad_enabled'] = isset($_POST[$prefix . '_splash_ad_enabled']);
    // TestMode UI removed — always use live unit IDs from AdMob / AdX fields
    $bundle['test_mode'] = false;
    $order = parsePriorityNumbers((string)($_POST[$prefix . '_onboardingScreenOrder'] ?? ''), 1, 23);
    if (count($order) > 23) {
        $order = array_slice($order, 0, 23);
    }
    $bundle['onboardingScreenOrder'] = $order !== [] ? $order : defaultOnboardingOrder();
    $bundle['click_interval'] = max(0, (int)($_POST[$prefix . '_click_interval'] ?? 1));
    $fs = parsePriorityNumbers((string)($_POST[$prefix . '_fullscreen_priority'] ?? '1,2,3,4'), 1, 4);
    $bundle['fullscreen_priority'] = $fs !== [] ? $fs : [1, 2, 3, 4];
    $bundle['launcherON'] = isset($_POST[$prefix . '_launcherON']);
    $bundle['launcher_interval'] = max(0, (int)($_POST[$prefix . '_launcher_interval'] ?? 1));
    $bundle['fb_app_id'] = trim((string)($_POST[$prefix . '_fb_app_id'] ?? ''));
    $bundle['fb_client_token'] = trim((string)($_POST[$prefix . '_fb_client_token'] ?? ''));
    foreach (['interstitial', 'rewarded', 'app_open', 'native'] as $fmt) {
        $bundle['priority'][$fmt] = parsePriorityNumbers((string)($_POST[$prefix . '_priority_' . $fmt] ?? ''), 1, 4);
    }
    foreach (['interstitial', 'rewarded', 'app_open'] as $fmt) {
        $bundle['fallback'][$fmt] = parsePriorityNumbers((string)($_POST[$prefix . '_fallback_' . $fmt] ?? ''), 1, 4);
    }
    foreach (['app_open_id', 'interstitial_id', 'rewarded_id', 'rewarded_interstitial_id', 'native_id', 'banner_id'] as $field) {
        $bundle['networks']['admob'][$field] = trim((string)($_POST[$prefix . '_admob_' . $field] ?? ''));
    }
    foreach (['app_open', 'interstitial', 'rewarded', 'rewarded_interstitial', 'native', 'banner'] as $fmt) {
        $bundle['networks']['adx'][$fmt] = parseAdxUnits($prefix, $fmt);
    }
    foreach (['app_id', 'app_open_id', 'interstitial_id', 'rewarded_id', 'native_id'] as $field) {
        $bundle['networks']['tradplus'][$field] = trim((string)($_POST[$prefix . '_tradplus_' . $field] ?? ''));
    }
    $bundle['networks']['custom'] = [
        'custom_url' => trim((string)($_POST[$prefix . '_custom_custom_url'] ?? '')),
    ];
    return $bundle;
}
