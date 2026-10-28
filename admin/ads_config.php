<?php
declare(strict_types=1);
require_once __DIR__ . '/config_store.php';
header('Content-Type: application/json; charset=utf-8');
header('Access-Control-Allow-Origin: *');
header('Cache-Control: no-store');
$settings = loadSettings();
$audience = strtolower(trim((string)($_GET['audience'] ?? 'organic')));
if ($audience !== 'marketing') {
    $audience = 'organic';
}
$organic = $settings['organic'] ?? emptyNetworkBundle();
$marketing = $settings['marketing'] ?? emptyNetworkBundle();
$cohort = $audience === 'marketing' ? $marketing : $organic;
// Flatten selected cohort at root + keep both cohorts so the app can pick by Install Referrer.
$payload = array_merge($cohort, [
    'ads_config_url' => $settings['ads_config_url'] ?? DEFAULT_API_BASE,
    'versionCode' => (int)($settings['versionCode'] ?? 1),
    'applink' => (string)($settings['applink'] ?? ''),
    'updateInprogress' => !empty($settings['updateInprogress']),
    'isSplashFullscreen' => !empty($settings['isSplashFullscreen']),
    'splashfullpriority' => array_values($settings['splashfullpriority'] ?? [1]),
    'afterCallON' => !empty($settings['afterCallON']),
    'whatsappAfterCall' => !empty($settings['whatsappAfterCall']),
    'afterCallShowPercent' => max(0, min(100, (int)($cohort['afterCallShowPercent'] ?? 100))),
    // Server flag; app still forces false for organic users
    'loanTrackerON' => $audience === 'marketing' && !empty($settings['loanTrackerON']),
    'onboardingScreenOrder' => array_values(
        $cohort['onboardingScreenOrder'] ?? defaultOnboardingOrder()
    ),
    'mainTest' => false,
    'userAudience' => $audience,
    'organic' => $organic,
    'marketing' => $marketing,
]);
echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
