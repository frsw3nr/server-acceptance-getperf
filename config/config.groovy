// 検査仕様シート定義

evidence.source = './監視設定チェックシート_Getperf.xlsx'
evidence.sheet_name_server = 'チェック対象'
evidence.sheet_name_rule = '検査ルール'
evidence.sheet_name_spec = [
    'Getperf':   '監視設定チェックシート(Getperf)',
]

// 検査結果ファイル出力先

evidence.target='./build/監視設定チェックシート(Getperf)_<date>.xlsx'

// 検査結果ログディレクトリ

evidence.staging_dir='./build/log'

// 並列化しないタスク
// 並列度を指定をしても、指定したドメインタスクはシリアルに実行する

test.serialization.tasks = ['Getperf']

// DryRunモードログ保存先

test.dry_run_staging_dir = './src/test/resource/log/'

// コマンド採取のタイムアウト
// Windows,vCenterの場合、全コマンドをまとめたバッチスクリプトのタイムアウト値

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// Cactiサーバ 接続情報

account.Getperf.Test.user     = 'psadmin'
account.Getperf.Test.password = 'psadmin'
account.Getperf.Test.work_dir = '/tmp/gradle_test'
