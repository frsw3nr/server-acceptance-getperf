// �����d�l�V�[�g��`

evidence.source = './�Ď��ݒ�`�F�b�N�V�[�g_Getperf.xlsx'
evidence.sheet_name_server = '�`�F�b�N�Ώ�'
evidence.sheet_name_rule = '�������[��'
evidence.sheet_name_spec = [
    'Getperf':   '�Ď��ݒ�`�F�b�N�V�[�g(Getperf)',
]

// �������ʃt�@�C���o�͐�

evidence.target='./build/�Ď��ݒ�`�F�b�N�V�[�g(Getperf)_<date>.xlsx'

// �������ʃ��O�f�B���N�g��

evidence.staging_dir='./build/log'

// ���񉻂��Ȃ��^�X�N
// ����x���w������Ă��A�w�肵���h���C���^�X�N�̓V���A���Ɏ��s����

test.serialization.tasks = ['Getperf']

// DryRun���[�h���O�ۑ���

test.dry_run_staging_dir = './src/test/resource/log/'

// �R�}���h�̎�̃^�C���A�E�g
// Windows,vCenter�̏ꍇ�A�S�R�}���h���܂Ƃ߂��o�b�`�X�N���v�g�̃^�C���A�E�g�l

test.Linux.timeout   = 30
test.Windows.timeout = 300
test.vCenter.timeout = 300

// Cacti�T�[�o �ڑ����

account.Getperf.Test.user     = 'psadmin'
account.Getperf.Test.password = 'psadmin'
account.Getperf.Test.work_dir = '/tmp/gradle_test'
