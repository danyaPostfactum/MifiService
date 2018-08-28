$(function(){
	init();
});
var cn = {
    "fail": "ʧ��",
    "pass": "�ɹ�"
   
};

var en = {
    "fail": "Fail",
    "pass": "Success"
    

};
function get_lan_device(m) {
    //��ȡ����
    var lan = getCookie('language');     //���԰汾
    //ѡȡ��������
    switch (lan) {
        case 'Chinese':
            var t = cn[m];
            break;
        default:
            var t = en[m];
    }

    //�����ѡ���Ե�json��û�д����ݾ�ѡȡ����������ʾ
    if (t == undefined) t = cn[m];
    if (t == undefined) t = en[m];


    if (t == undefined) t = m; //�������û�оͷ������ı�ʶ

    return t;
}
function init() {

    var param = { funcNo: 1029 };
    request(param, function(data) {
        var flag = data.flag;
        var error_info = data.error_info;

        if (flag == "1") {//��ȷ
            var result = data.results[0];
            //������Ҫ������
            
            $("#imei").val(result.imei);
            $("#FWversion").html(result.fwversion);
            $("#manufacturer").html(result.manufacture);
            $("#cputemp").html(result.cputemp);
            $("#dbmSignal").html(result.dbm);
                 
        } else {//����
             Alert(mifi_translate(error_info)); 
        }
    });
   
	//$("#imei").html($.session.get("imei"));
	//$("#FWversion").html($.session.get("fwversion"));
}

$("#apply").bind('click', function(e) {

    var nImei = $("#imei").val();
  
//    var reg = /^[0-9]$/;

//    if (!reg.test(nImei)) {
//        Alert("Invalid IMEI!");
//        return;
//    }

    var param = { "funcNo": 2002,
         "imei": nImei
     
    };
    request(param, function(data) {
        var flag = data.flag;
        var error_info = data.error_info;

        if (flag == "1") {//��ȷ

            Alert(mifi_translate('pass'));
            return;
        } else {//����
            Alert(mifi_translate('fail'));
			return;
        }
        //location.reload(true);
    });
});
