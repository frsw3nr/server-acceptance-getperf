Getperf(Cacti) 監視設定収集ツール
=================================

システム概要
------------

Getperf(Cacti)監視サーバにて監視エージェントの状態を収集します。


**Note**

* 本ツールはコミュニティ版 [Getperf](https://github.com/getperf/getperf) 環境で利用可能です。
旧監視環境での収集します。

システム要件
------------

**検査対象サーバ**

* Getperf サーバに SSH接続できる環境が必要です。

**検査用PC**

* server-acceptance 本体が利用できる環境が必要です。
* 以下の通り、**システム環境変数 にserver-acceptanceのホームディレクトリのパス**を設定します。
    * スタート→コンピューター→コンピューターを右クリック→プロパティをクリックします。
    * システムの詳細設定を順にたどり、システムのプロパティを表示します。
    * 環境変数ボタンをクリックします。
    * システム環境変数のリストボックスの中からPathの行を選択して、編集ボタンをクリックします。
    * ;(セミコロン)で区切って行の最後にserver-acceptance のパスを追加します。
    * 設定が終わったら、OKボタンをクリックします。

利用方法
--------

1. 7-zip を用いて、 server-acceptance-getperf.zip を解凍します。
2. 「監視設定チェックシート_getperf.xlsx」を開き、シート「チェック対象」に以下記入をします。
    * 監視エージェントのサーバ名
    * Cacti監視サーバのアドレス
    * 'Getperf'固定
    * Cacti監視サーバのアカウントID。設定ファイル内に記述
    * Cacti監視サーバのエージェント定義名
3. config/config.groovy 内のサーバアカウント情報を編集します。

        // Getperf接続情報
        account.Getperf.Test.user     = 'admin'
        account.Getperf.Test.password = 'getperf'

4. 解凍したディレクトリに移動し、getconfig コマンドを実行します。使用方法は以下の通りです。

        getconfig

5. 全ホストを対象に検査をする場合は、「監視設定チェックシート_GetperfAll.xlsx」を編集して以下を実行してください。

        getconfig -e 監視設定チェックシート_GetperfAll.xlsx

Reference
---------

* [Getperf](https://github.com/getperf/getperf)

AUTHOR
-----------

Minoru Furusawa <minoru.furusawa@toshiba.co.jp>

COPYRIGHT
-----------

Copyright 2014-2016, Minoru Furusawa, Toshiba corporation.
