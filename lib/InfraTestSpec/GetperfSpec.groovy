package InfraTestSpec

import groovy.util.logging.Slf4j
import groovy.transform.InheritConstructors
import org.hidetake.groovy.ssh.Ssh
import jp.co.toshiba.ITInfra.acceptance.InfraTestSpec.*
import jp.co.toshiba.ITInfra.acceptance.*


@Slf4j
@InheritConstructors
class GetperfSpec extends LinuxSpecBase {

    String agent
    int    timeout = 30
    def    current = new Date().format("yyyyMMdd")

    def init() {
        super.init()
        def remote_account = test_server.remote_account
        this.agent = test_server.remote_alias
    }

    def finish() {
        super.finish()
    }

    def get_site_homes(session) {
        def test_id = 'site_homes_' + ip
        if (!Config.instance.configs.containsKey('site_homes')) {
            def site_homes = [:]
            def lines_site_home = exec(test_id, true) {
                def command = "grep -H home /home/psadmin/getperf/config/site/*"
                run_ssh_command(session, command, test_id, true)
            }
            lines_site_home.eachLine {
                ( it =~ /\/site\/(.+?)\.json:\s+"home":\s+"(.+?)"/).each {
                    m0, sitekey, site_home ->
                    site_homes[sitekey] = site_home
                }
            }
            Config.instance.configs['site_homes'] = site_homes
        }
        return Config.instance.configs['site_homes']
    }

    def get_last_updates(session) {
        def site_homes = get_site_homes(session)
        if (!Config.instance.configs.containsKey('last_updates')) {
            def last_updates = [:].withDefault{[:]}
            site_homes.each { sitekey, site_home ->
                def test_id = 'node_summarys_' + sitekey
                def lines = exec(test_id, true) {
                    def command = "find ${site_home}/summary/  -maxdepth 3 -mindepth 3"
                    run_ssh_command(session, command, test_id, true)
                }
                if (lines) {
                    lines.eachLine { line->
                        ( line =~ /\/summary\/(.+?)\/(.+?)\/(\d+?)$/ ).each {
                            m0, node, domain, transfer_date->
                            last_updates[node][domain] = transfer_date
                        }
                    }
                }
            }
            Config.instance.configs['last_updates'] = last_updates
        }
        return Config.instance.configs['last_updates']
    }

    def ssl_expire(session, test_item) {
        def lines = exec('ssl_expire') {
            def host_dir = (this.agent) ?: '*'
            def ssl_path = "/etc/getperf/ssl/client/*/${host_dir}/network/License.txt"
            // def command = """\
            //     |if [ -e ${ssl_path} ]; then
            //     |   grep -H EXPIRE ${ssl_path}
            //     |fi
            // """.stripMargin()

            def command = """\
            |if [ -f /etc/machine-id ]; then
            |    cat /etc/machine-id > ${work_dir}/ssl_expire
            |elif [ -f /var/lib/dbus/machine-id ]; then
            |    cat /var/lib/dbus/machine-id > ${work_dir}/ssl_expire
            |fi
            """.stripMargin()
            session.execute command

println command
            run_ssh_command(session, command, 'ssl_expire')
        }
        def last_expired = 'Unkown'
        def csv = []
        lines.eachLine {
            ( it =~ /client\/(.+?)\/(.+?)\/network\/License\.txt:EXPIRE=(\d+)$/).each {
                m0, sitekey, agent_name, expired->
                last_expired = expired
                csv << [sitekey, agent_name, expired]
            }
        }
        def headers = ['Sitekey', 'AgentName', 'Expired']
        test_item.devices(csv, headers)
println "${current},${last_expired}"
        if (last_expired == 'Unkown') {
            test_item.results("Unkown")
        } else if (current <= last_expired) {
            test_item.results("Not Expired")
            test_item.verify_status(true)
        } else {
            test_item.results("Expired")
            test_item.verify_status(false)
        }
    }

    def analysis(session, test_item) {
        def site_homes = get_site_homes(session)
        def last_transfer_dates = [:]
        def csv = []
        def lines = exec("ls_analysis") {
            def ls_analysis = ''
            site_homes.each { sitekey, site_home ->
                def log_file  = "${work_dir}/ls_analysis"
                def host_dir1 = (this.agent) ?: ''
                def host_dir2 = (this.agent) ?: '*'
                def command = """\
                    |if [ -d ${site_home}/analysis/${host_dir1} ]; then
                    |   ls ${site_home}/analysis/${host_dir2}/*/* -d
                    |fi
                """.stripMargin()
                def result = session.execute command, ignoreError: true
                if (result != '') {
                    ls_analysis += result + "\n"
                }
            }
            new File("${local_dir}/ls_analysis").text = ls_analysis
        }
        lines.eachLine { line->
            ( line =~ /\/(.+?)\/analysis\/(.+?)\/(.+?)\/(.+?)$/ ).each {
                m0, site_home, hostname, domain, transfer_date->
                csv << [site_home, hostname, domain, transfer_date]
                last_transfer_dates[domain] = transfer_date
            }
        }
        def headers = ['SiteHome', 'Hostname', 'Domain', 'TransferDate']
        test_item.devices(csv, headers)
println transfer_date
        test_item.results(last_transfer_dates.toString())
    }

    def domain(session, test_item) {
        def last_updates = get_last_updates(session)
        if (last_updates.containsKey(this.agent)) {
            def results = [:]
            last_updates[this.agent].each { domain, date ->
                results["domain.${domain}"] = date
            test_item.results(results)
            }
        } else {
            test_item.results("Not Found")
        }
    }
}
