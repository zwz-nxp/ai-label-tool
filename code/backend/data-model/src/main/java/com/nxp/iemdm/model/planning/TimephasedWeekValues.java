package com.nxp.iemdm.model.planning;

public interface TimephasedWeekValues {

  int MIN_WEEK_NUMBER = 1;
  int MAX_WEEK_NUMBER = 107;
  String WEEK_NUMBER_ERROR_MESSAGE =
      "Number %d is not a valid week number for time-phased week values";

  default void setAllWeekValues(double value) {
    for (int weekNumber = MIN_WEEK_NUMBER; weekNumber <= MAX_WEEK_NUMBER; weekNumber++) {
      this.setWeekValue(weekNumber, value);
    }
  }

  default void addWeekValue(int weekNumber, double value) {
    switch (weekNumber) {
      case 1 -> this.setWeek1(this.getWeek1() + value);
      case 2 -> this.setWeek2(this.getWeek2() + value);
      case 3 -> this.setWeek3(this.getWeek3() + value);
      case 4 -> this.setWeek4(this.getWeek4() + value);
      case 5 -> this.setWeek5(this.getWeek5() + value);
      case 6 -> this.setWeek6(this.getWeek6() + value);
      case 7 -> this.setWeek7(this.getWeek7() + value);
      case 8 -> this.setWeek8(this.getWeek8() + value);
      case 9 -> this.setWeek9(this.getWeek9() + value);
      case 10 -> this.setWeek10(this.getWeek10() + value);
      case 11 -> this.setWeek11(this.getWeek11() + value);
      case 12 -> this.setWeek12(this.getWeek12() + value);
      case 13 -> this.setWeek13(this.getWeek13() + value);
      case 14 -> this.setWeek14(this.getWeek14() + value);
      case 15 -> this.setWeek15(this.getWeek15() + value);
      case 16 -> this.setWeek16(this.getWeek16() + value);
      case 17 -> this.setWeek17(this.getWeek17() + value);
      case 18 -> this.setWeek18(this.getWeek18() + value);
      case 19 -> this.setWeek19(this.getWeek19() + value);
      case 20 -> this.setWeek20(this.getWeek20() + value);
      case 21 -> this.setWeek21(this.getWeek21() + value);
      case 22 -> this.setWeek22(this.getWeek22() + value);
      case 23 -> this.setWeek23(this.getWeek23() + value);
      case 24 -> this.setWeek24(this.getWeek24() + value);
      case 25 -> this.setWeek25(this.getWeek25() + value);
      case 26 -> this.setWeek26(this.getWeek26() + value);
      case 27 -> this.setWeek27(this.getWeek27() + value);
      case 28 -> this.setWeek28(this.getWeek28() + value);
      case 29 -> this.setWeek29(this.getWeek29() + value);
      case 30 -> this.setWeek30(this.getWeek30() + value);
      case 31 -> this.setWeek31(this.getWeek31() + value);
      case 32 -> this.setWeek32(this.getWeek32() + value);
      case 33 -> this.setWeek33(this.getWeek33() + value);
      case 34 -> this.setWeek34(this.getWeek34() + value);
      case 35 -> this.setWeek35(this.getWeek35() + value);
      case 36 -> this.setWeek36(this.getWeek36() + value);
      case 37 -> this.setWeek37(this.getWeek37() + value);
      case 38 -> this.setWeek38(this.getWeek38() + value);
      case 39 -> this.setWeek39(this.getWeek39() + value);
      case 40 -> this.setWeek40(this.getWeek40() + value);
      case 41 -> this.setWeek41(this.getWeek41() + value);
      case 42 -> this.setWeek42(this.getWeek42() + value);
      case 43 -> this.setWeek43(this.getWeek43() + value);
      case 44 -> this.setWeek44(this.getWeek44() + value);
      case 45 -> this.setWeek45(this.getWeek45() + value);
      case 46 -> this.setWeek46(this.getWeek46() + value);
      case 47 -> this.setWeek47(this.getWeek47() + value);
      case 48 -> this.setWeek48(this.getWeek48() + value);
      case 49 -> this.setWeek49(this.getWeek49() + value);
      case 50 -> this.setWeek50(this.getWeek50() + value);
      case 51 -> this.setWeek51(this.getWeek51() + value);
      case 52 -> this.setWeek52(this.getWeek52() + value);
      case 53 -> this.setWeek53(this.getWeek53() + value);
      case 54 -> this.setWeek54(this.getWeek54() + value);
      case 55 -> this.setWeek55(this.getWeek55() + value);
      case 56 -> this.setWeek56(this.getWeek56() + value);
      case 57 -> this.setWeek57(this.getWeek57() + value);
      case 58 -> this.setWeek58(this.getWeek58() + value);
      case 59 -> this.setWeek59(this.getWeek59() + value);
      case 60 -> this.setWeek60(this.getWeek60() + value);
      case 61 -> this.setWeek61(this.getWeek61() + value);
      case 62 -> this.setWeek62(this.getWeek62() + value);
      case 63 -> this.setWeek63(this.getWeek63() + value);
      case 64 -> this.setWeek64(this.getWeek64() + value);
      case 65 -> this.setWeek65(this.getWeek65() + value);
      case 66 -> this.setWeek66(this.getWeek66() + value);
      case 67 -> this.setWeek67(this.getWeek67() + value);
      case 68 -> this.setWeek68(this.getWeek68() + value);
      case 69 -> this.setWeek69(this.getWeek69() + value);
      case 70 -> this.setWeek70(this.getWeek70() + value);
      case 71 -> this.setWeek71(this.getWeek71() + value);
      case 72 -> this.setWeek72(this.getWeek72() + value);
      case 73 -> this.setWeek73(this.getWeek73() + value);
      case 74 -> this.setWeek74(this.getWeek74() + value);
      case 75 -> this.setWeek75(this.getWeek75() + value);
      case 76 -> this.setWeek76(this.getWeek76() + value);
      case 77 -> this.setWeek77(this.getWeek77() + value);
      case 78 -> this.setWeek78(this.getWeek78() + value);
      case 79 -> this.setWeek79(this.getWeek79() + value);
      case 80 -> this.setWeek80(this.getWeek80() + value);
      case 81 -> this.setWeek81(this.getWeek81() + value);
      case 82 -> this.setWeek82(this.getWeek82() + value);
      case 83 -> this.setWeek83(this.getWeek83() + value);
      case 84 -> this.setWeek84(this.getWeek84() + value);
      case 85 -> this.setWeek85(this.getWeek85() + value);
      case 86 -> this.setWeek86(this.getWeek86() + value);
      case 87 -> this.setWeek87(this.getWeek87() + value);
      case 88 -> this.setWeek88(this.getWeek88() + value);
      case 89 -> this.setWeek89(this.getWeek89() + value);
      case 90 -> this.setWeek90(this.getWeek90() + value);
      case 91 -> this.setWeek91(this.getWeek91() + value);
      case 92 -> this.setWeek92(this.getWeek92() + value);
      case 93 -> this.setWeek93(this.getWeek93() + value);
      case 94 -> this.setWeek94(this.getWeek94() + value);
      case 95 -> this.setWeek95(this.getWeek95() + value);
      case 96 -> this.setWeek96(this.getWeek96() + value);
      case 97 -> this.setWeek97(this.getWeek97() + value);
      case 98 -> this.setWeek98(this.getWeek98() + value);
      case 99 -> this.setWeek99(this.getWeek99() + value);
      case 100 -> this.setWeek100(this.getWeek100() + value);
      case 101 -> this.setWeek101(this.getWeek101() + value);
      case 102 -> this.setWeek102(this.getWeek102() + value);
      case 103 -> this.setWeek103(this.getWeek103() + value);
      case 104 -> this.setWeek104(this.getWeek104() + value);
      case 105 -> this.setWeek105(this.getWeek105() + value);
      case 106 -> this.setWeek106(this.getWeek106() + value);
      case 107 -> this.setWeek107(this.getWeek107() + value);
      default ->
          throw new IllegalArgumentException(String.format(WEEK_NUMBER_ERROR_MESSAGE, weekNumber));
    }
  }

  default double getWeekValue(int weekNumber) {
    return switch (weekNumber) {
      case 1 -> this.getWeek1();
      case 2 -> this.getWeek2();
      case 3 -> this.getWeek3();
      case 4 -> this.getWeek4();
      case 5 -> this.getWeek5();
      case 6 -> this.getWeek6();
      case 7 -> this.getWeek7();
      case 8 -> this.getWeek8();
      case 9 -> this.getWeek9();
      case 10 -> this.getWeek10();
      case 11 -> this.getWeek11();
      case 12 -> this.getWeek12();
      case 13 -> this.getWeek13();
      case 14 -> this.getWeek14();
      case 15 -> this.getWeek15();
      case 16 -> this.getWeek16();
      case 17 -> this.getWeek17();
      case 18 -> this.getWeek18();
      case 19 -> this.getWeek19();
      case 20 -> this.getWeek20();
      case 21 -> this.getWeek21();
      case 22 -> this.getWeek22();
      case 23 -> this.getWeek23();
      case 24 -> this.getWeek24();
      case 25 -> this.getWeek25();
      case 26 -> this.getWeek26();
      case 27 -> this.getWeek27();
      case 28 -> this.getWeek28();
      case 29 -> this.getWeek29();
      case 30 -> this.getWeek30();
      case 31 -> this.getWeek31();
      case 32 -> this.getWeek32();
      case 33 -> this.getWeek33();
      case 34 -> this.getWeek34();
      case 35 -> this.getWeek35();
      case 36 -> this.getWeek36();
      case 37 -> this.getWeek37();
      case 38 -> this.getWeek38();
      case 39 -> this.getWeek39();
      case 40 -> this.getWeek40();
      case 41 -> this.getWeek41();
      case 42 -> this.getWeek42();
      case 43 -> this.getWeek43();
      case 44 -> this.getWeek44();
      case 45 -> this.getWeek45();
      case 46 -> this.getWeek46();
      case 47 -> this.getWeek47();
      case 48 -> this.getWeek48();
      case 49 -> this.getWeek49();
      case 50 -> this.getWeek50();
      case 51 -> this.getWeek51();
      case 52 -> this.getWeek52();
      case 53 -> this.getWeek53();
      case 54 -> this.getWeek54();
      case 55 -> this.getWeek55();
      case 56 -> this.getWeek56();
      case 57 -> this.getWeek57();
      case 58 -> this.getWeek58();
      case 59 -> this.getWeek59();
      case 60 -> this.getWeek60();
      case 61 -> this.getWeek61();
      case 62 -> this.getWeek62();
      case 63 -> this.getWeek63();
      case 64 -> this.getWeek64();
      case 65 -> this.getWeek65();
      case 66 -> this.getWeek66();
      case 67 -> this.getWeek67();
      case 68 -> this.getWeek68();
      case 69 -> this.getWeek69();
      case 70 -> this.getWeek70();
      case 71 -> this.getWeek71();
      case 72 -> this.getWeek72();
      case 73 -> this.getWeek73();
      case 74 -> this.getWeek74();
      case 75 -> this.getWeek75();
      case 76 -> this.getWeek76();
      case 77 -> this.getWeek77();
      case 78 -> this.getWeek78();
      case 79 -> this.getWeek79();
      case 80 -> this.getWeek80();
      case 81 -> this.getWeek81();
      case 82 -> this.getWeek82();
      case 83 -> this.getWeek83();
      case 84 -> this.getWeek84();
      case 85 -> this.getWeek85();
      case 86 -> this.getWeek86();
      case 87 -> this.getWeek87();
      case 88 -> this.getWeek88();
      case 89 -> this.getWeek89();
      case 90 -> this.getWeek90();
      case 91 -> this.getWeek91();
      case 92 -> this.getWeek92();
      case 93 -> this.getWeek93();
      case 94 -> this.getWeek94();
      case 95 -> this.getWeek95();
      case 96 -> this.getWeek96();
      case 97 -> this.getWeek97();
      case 98 -> this.getWeek98();
      case 99 -> this.getWeek99();
      case 100 -> this.getWeek100();
      case 101 -> this.getWeek101();
      case 102 -> this.getWeek102();
      case 103 -> this.getWeek103();
      case 104 -> this.getWeek104();
      case 105 -> this.getWeek105();
      case 106 -> this.getWeek106();
      case 107 -> this.getWeek107();
      default ->
          throw new IllegalArgumentException(String.format(WEEK_NUMBER_ERROR_MESSAGE, weekNumber));
    };
  }

  default void setWeekValue(int weekNumber, double value) {
    switch (weekNumber) {
      case 1 -> this.setWeek1(value);
      case 2 -> this.setWeek2(value);
      case 3 -> this.setWeek3(value);
      case 4 -> this.setWeek4(value);
      case 5 -> this.setWeek5(value);
      case 6 -> this.setWeek6(value);
      case 7 -> this.setWeek7(value);
      case 8 -> this.setWeek8(value);
      case 9 -> this.setWeek9(value);
      case 10 -> this.setWeek10(value);
      case 11 -> this.setWeek11(value);
      case 12 -> this.setWeek12(value);
      case 13 -> this.setWeek13(value);
      case 14 -> this.setWeek14(value);
      case 15 -> this.setWeek15(value);
      case 16 -> this.setWeek16(value);
      case 17 -> this.setWeek17(value);
      case 18 -> this.setWeek18(value);
      case 19 -> this.setWeek19(value);
      case 20 -> this.setWeek20(value);
      case 21 -> this.setWeek21(value);
      case 22 -> this.setWeek22(value);
      case 23 -> this.setWeek23(value);
      case 24 -> this.setWeek24(value);
      case 25 -> this.setWeek25(value);
      case 26 -> this.setWeek26(value);
      case 27 -> this.setWeek27(value);
      case 28 -> this.setWeek28(value);
      case 29 -> this.setWeek29(value);
      case 30 -> this.setWeek30(value);
      case 31 -> this.setWeek31(value);
      case 32 -> this.setWeek32(value);
      case 33 -> this.setWeek33(value);
      case 34 -> this.setWeek34(value);
      case 35 -> this.setWeek35(value);
      case 36 -> this.setWeek36(value);
      case 37 -> this.setWeek37(value);
      case 38 -> this.setWeek38(value);
      case 39 -> this.setWeek39(value);
      case 40 -> this.setWeek40(value);
      case 41 -> this.setWeek41(value);
      case 42 -> this.setWeek42(value);
      case 43 -> this.setWeek43(value);
      case 44 -> this.setWeek44(value);
      case 45 -> this.setWeek45(value);
      case 46 -> this.setWeek46(value);
      case 47 -> this.setWeek47(value);
      case 48 -> this.setWeek48(value);
      case 49 -> this.setWeek49(value);
      case 50 -> this.setWeek50(value);
      case 51 -> this.setWeek51(value);
      case 52 -> this.setWeek52(value);
      case 53 -> this.setWeek53(value);
      case 54 -> this.setWeek54(value);
      case 55 -> this.setWeek55(value);
      case 56 -> this.setWeek56(value);
      case 57 -> this.setWeek57(value);
      case 58 -> this.setWeek58(value);
      case 59 -> this.setWeek59(value);
      case 60 -> this.setWeek60(value);
      case 61 -> this.setWeek61(value);
      case 62 -> this.setWeek62(value);
      case 63 -> this.setWeek63(value);
      case 64 -> this.setWeek64(value);
      case 65 -> this.setWeek65(value);
      case 66 -> this.setWeek66(value);
      case 67 -> this.setWeek67(value);
      case 68 -> this.setWeek68(value);
      case 69 -> this.setWeek69(value);
      case 70 -> this.setWeek70(value);
      case 71 -> this.setWeek71(value);
      case 72 -> this.setWeek72(value);
      case 73 -> this.setWeek73(value);
      case 74 -> this.setWeek74(value);
      case 75 -> this.setWeek75(value);
      case 76 -> this.setWeek76(value);
      case 77 -> this.setWeek77(value);
      case 78 -> this.setWeek78(value);
      case 79 -> this.setWeek79(value);
      case 80 -> this.setWeek80(value);
      case 81 -> this.setWeek81(value);
      case 82 -> this.setWeek82(value);
      case 83 -> this.setWeek83(value);
      case 84 -> this.setWeek84(value);
      case 85 -> this.setWeek85(value);
      case 86 -> this.setWeek86(value);
      case 87 -> this.setWeek87(value);
      case 88 -> this.setWeek88(value);
      case 89 -> this.setWeek89(value);
      case 90 -> this.setWeek90(value);
      case 91 -> this.setWeek91(value);
      case 92 -> this.setWeek92(value);
      case 93 -> this.setWeek93(value);
      case 94 -> this.setWeek94(value);
      case 95 -> this.setWeek95(value);
      case 96 -> this.setWeek96(value);
      case 97 -> this.setWeek97(value);
      case 98 -> this.setWeek98(value);
      case 99 -> this.setWeek99(value);
      case 100 -> this.setWeek100(value);
      case 101 -> this.setWeek101(value);
      case 102 -> this.setWeek102(value);
      case 103 -> this.setWeek103(value);
      case 104 -> this.setWeek104(value);
      case 105 -> this.setWeek105(value);
      case 106 -> this.setWeek106(value);
      case 107 -> this.setWeek107(value);
      default ->
          throw new IllegalArgumentException(String.format(WEEK_NUMBER_ERROR_MESSAGE, weekNumber));
    }
  }

  double getWeek1();

  double getWeek2();

  double getWeek3();

  double getWeek4();

  double getWeek5();

  double getWeek6();

  double getWeek7();

  double getWeek8();

  double getWeek9();

  double getWeek10();

  double getWeek11();

  double getWeek12();

  double getWeek13();

  double getWeek14();

  double getWeek15();

  double getWeek16();

  double getWeek17();

  double getWeek18();

  double getWeek19();

  double getWeek20();

  double getWeek21();

  double getWeek22();

  double getWeek23();

  double getWeek24();

  double getWeek25();

  double getWeek26();

  double getWeek27();

  double getWeek28();

  double getWeek29();

  double getWeek30();

  double getWeek31();

  double getWeek32();

  double getWeek33();

  double getWeek34();

  double getWeek35();

  double getWeek36();

  double getWeek37();

  double getWeek38();

  double getWeek39();

  double getWeek40();

  double getWeek41();

  double getWeek42();

  double getWeek43();

  double getWeek44();

  double getWeek45();

  double getWeek46();

  double getWeek47();

  double getWeek48();

  double getWeek49();

  double getWeek50();

  double getWeek51();

  double getWeek52();

  double getWeek53();

  double getWeek54();

  double getWeek55();

  double getWeek56();

  double getWeek57();

  double getWeek58();

  double getWeek59();

  double getWeek60();

  double getWeek61();

  double getWeek62();

  double getWeek63();

  double getWeek64();

  double getWeek65();

  double getWeek66();

  double getWeek67();

  double getWeek68();

  double getWeek69();

  double getWeek70();

  double getWeek71();

  double getWeek72();

  double getWeek73();

  double getWeek74();

  double getWeek75();

  double getWeek76();

  double getWeek77();

  double getWeek78();

  double getWeek79();

  double getWeek80();

  double getWeek81();

  double getWeek82();

  double getWeek83();

  double getWeek84();

  double getWeek85();

  double getWeek86();

  double getWeek87();

  double getWeek88();

  double getWeek89();

  double getWeek90();

  double getWeek91();

  double getWeek92();

  double getWeek93();

  double getWeek94();

  double getWeek95();

  double getWeek96();

  double getWeek97();

  double getWeek98();

  double getWeek99();

  double getWeek100();

  double getWeek101();

  double getWeek102();

  double getWeek103();

  double getWeek104();

  double getWeek105();

  double getWeek106();

  double getWeek107();

  void setWeek1(double week1);

  void setWeek2(double week2);

  void setWeek3(double week3);

  void setWeek4(double week4);

  void setWeek5(double week5);

  void setWeek6(double week6);

  void setWeek7(double week7);

  void setWeek8(double week8);

  void setWeek9(double week9);

  void setWeek10(double week10);

  void setWeek11(double week11);

  void setWeek12(double week12);

  void setWeek13(double week13);

  void setWeek14(double week14);

  void setWeek15(double week15);

  void setWeek16(double week16);

  void setWeek17(double week17);

  void setWeek18(double week18);

  void setWeek19(double week19);

  void setWeek20(double week20);

  void setWeek21(double week21);

  void setWeek22(double week22);

  void setWeek23(double week23);

  void setWeek24(double week24);

  void setWeek25(double week25);

  void setWeek26(double week26);

  void setWeek27(double week27);

  void setWeek28(double week28);

  void setWeek29(double week29);

  void setWeek30(double week30);

  void setWeek31(double week31);

  void setWeek32(double week32);

  void setWeek33(double week33);

  void setWeek34(double week34);

  void setWeek35(double week35);

  void setWeek36(double week36);

  void setWeek37(double week37);

  void setWeek38(double week38);

  void setWeek39(double week39);

  void setWeek40(double week40);

  void setWeek41(double week41);

  void setWeek42(double week42);

  void setWeek43(double week43);

  void setWeek44(double week44);

  void setWeek45(double week45);

  void setWeek46(double week46);

  void setWeek47(double week47);

  void setWeek48(double week48);

  void setWeek49(double week49);

  void setWeek50(double week50);

  void setWeek51(double week51);

  void setWeek52(double week52);

  void setWeek53(double week53);

  void setWeek54(double week54);

  void setWeek55(double week55);

  void setWeek56(double week56);

  void setWeek57(double week57);

  void setWeek58(double week58);

  void setWeek59(double week59);

  void setWeek60(double week60);

  void setWeek61(double week61);

  void setWeek62(double week62);

  void setWeek63(double week63);

  void setWeek64(double week64);

  void setWeek65(double week65);

  void setWeek66(double week66);

  void setWeek67(double week67);

  void setWeek68(double week68);

  void setWeek69(double week69);

  void setWeek70(double week70);

  void setWeek71(double week71);

  void setWeek72(double week72);

  void setWeek73(double week73);

  void setWeek74(double week74);

  void setWeek75(double week75);

  void setWeek76(double week76);

  void setWeek77(double week77);

  void setWeek78(double week78);

  void setWeek79(double week79);

  void setWeek80(double week80);

  void setWeek81(double week81);

  void setWeek82(double week82);

  void setWeek83(double week83);

  void setWeek84(double week84);

  void setWeek85(double week85);

  void setWeek86(double week86);

  void setWeek87(double week87);

  void setWeek88(double week88);

  void setWeek89(double week89);

  void setWeek90(double week90);

  void setWeek91(double week91);

  void setWeek92(double week92);

  void setWeek93(double week93);

  void setWeek94(double week94);

  void setWeek95(double week95);

  void setWeek96(double week96);

  void setWeek97(double week97);

  void setWeek98(double week98);

  void setWeek99(double week99);

  void setWeek100(double week100);

  void setWeek101(double week101);

  void setWeek102(double week102);

  void setWeek103(double week103);

  void setWeek104(double week104);

  void setWeek105(double week105);

  void setWeek106(double week106);

  void setWeek107(double week107);
}
