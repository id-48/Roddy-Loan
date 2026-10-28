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
$cohort = $settings[$audience] ?? emptyNetworkBundle();
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
    'loanTrackerON' => !empty($settings['loanTrackerON']),
    'onboardingScreenOrder' => array_values(
        $cohort['onboardingScreenOrder'] ?? defaultOnboardingOrder()
    ),
    'mainTest' => false,
    'userAudience' => $audience,
    'organic' => $settings['organic'] ?? emptyNetworkBundle(),
    'marketing' => $settings['marketing'] ?? emptyNetworkBundle(),
]);
echo json_encode($payload, JSON_UNESCAPED_SLASHES | JSON_PRETTY_PRINT);
