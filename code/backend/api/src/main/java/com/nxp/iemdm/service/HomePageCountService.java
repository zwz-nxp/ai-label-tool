package com.nxp.iemdm.service;

import com.nxp.iemdm.model.notification.HomePageCount;

public interface HomePageCountService {
  HomePageCount getHomePageCount(String wbi, String maxNotifications);
}
