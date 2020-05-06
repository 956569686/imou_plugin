

import 'package:flutter/material.dart';

class ConnectStatusModel with ChangeNotifier {

  String _event;
  String _value;

  String get event => _event;

  String get value => _value;

  setConnectStatus(String event,String value){
    _event = event;
    _value = value;
    notifyListeners();
  }


}