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

    def init() {
        super.init()
        def remote_account = test_server.remote_account
        this.agent = test_server.remote_alias
        // timeout          = test_server.timeout
    }

    def finish() {
        super.finish()
    }

    def ssl_expire(session, test_item) {
        def lines = exec('ssl_expire') {
            def host_dir = (this.agent) ?: '*'
            def ssl_path = "/etc/getperf/ssl/client/*/${host_dir}/network/License.txt"
            run_ssh_command(session, "grep -H EXPIRE ${ssl_path}", 'ssl_expire')
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
        test_item.results(last_expired)
    }

    def data_transfer(session, test_item) {
        def lines_site_home = exec('site_home') {
            def command = "grep -H home /home/psadmin/getperf/config/site/*"
            run_ssh_command(session, command, 'site_home')
        }
        def site_homes = [:]
        lines_site_home.eachLine {
            ( it =~ /\/site\/(.+?)\.json:\s+"home":\s+"(.+?)"/).each {
                m0, sitekey, site_home ->
                site_homes[sitekey] = site_home
            }
        }
        def last_transfer_dates = [:]
        def csv = []
        site_homes.each { sitekey, site_home ->
            def log_file = "ls_analysis_${sitekey}"
            def lines = exec(log_file) {
                def host_dir = (this.agent) ?: '*'
                def command = "ls ${site_home}/analysis/${host_dir}/*/* -d"
                run_ssh_command(session, command, log_file)
            }
            lines.eachLine { line->
                ( line =~ /\/analysis\/(.+?)\/(.+?)\/(.+?)$/ ).each {
                    m0, hostname, domain, transfer_date->
                    csv << [sitekey, site_home, hostname, domain, transfer_date]
                    last_transfer_dates[domain] = transfer_date
                }
            }
        }
        def headers = ['Sitekey', 'SiteHome', 'Hostname', 'Domain', 'TransferDate']
        test_item.devices(csv, headers)
        test_item.results(last_transfer_dates.toString())
    }

    def domain(session, test_item) {
        def lines_site_home = exec('site_home') {
            def command = "grep -H home /home/psadmin/getperf/config/site/*"
            run_ssh_command(session, command, 'site_home')
        }
        def site_homes = [:]
        lines_site_home.eachLine {
            ( it =~ /\/site\/(.+?)\.json:\s+"home":\s+"(.+?)"/).each {
                m0, sitekey, site_home ->
                site_homes[sitekey] = site_home
            }
        }
        def last_transfer_dates = [:]
        def csv = []
        site_homes.each { sitekey, site_home ->
            def log_file = "ls_node_${sitekey}"
            def lines = exec(log_file) {
                def paths = []
                if (this.agent) {
                    paths.add("${site_home}/node/*/${this.agent}/")
                    def agent_uc = this.agent.toUpperCase()
                    if (agent_uc != this.agent)
                        paths.add("${site_home}/node/*/${agent_uc}/")
                } else {
                    paths.add("${site_home}/node/*/*/")
                }
                def command = "ls  -l -u -d --time-style=+%Y-%m-%d " + paths.join(' ')
                command +=  ">> ${work_dir}/${log_file}"
                session.execute command, ignoreError : true
                session.get from: "${work_dir}/${log_file}", into: local_dir
                new File("${local_dir}/${log_file}").text
            }
// drwxr-xr-x 4 psadmin cacti 4096 2016-12-05 /catai/peyok02/node/Linux/yqaj222/
            lines.eachLine { line->
                ( line =~ /(\d+-\d+-\d+) .+\/(.+?)\/(.+?)\/$/ ).each {
                    m0, transfer_date, domain, node->
                    csv << [sitekey, site_home, node, domain, transfer_date]
                    last_transfer_dates['domain.' + domain] = transfer_date
                }
            }
        }
        def headers = ['Sitekey', 'SiteHome', 'Nodename', 'Domain', 'TransferDate']
        test_item.devices(csv, headers)
        last_transfer_dates['domain'] = last_transfer_dates.size()
        test_item.results(last_transfer_dates)
    }

}
