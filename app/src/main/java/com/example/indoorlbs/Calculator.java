package com.example.indoorlbs;

public class Calculator {

    // 가속도 센서 변수
    private double acc_X, acc_Y, acc_Z;

    // 가속도 벡터 합 변수
    private double accVector;


    // HPF시 필요한 변수
    private double preAcc;
    private double newAcc;

    private double[] HFilter_data;
    private int HFData_amount;
    private int adjustValue;


    // LPF시 필요한 변수
    private double LFilter_data[];
    private int LFilterData_amount;

    // Zero-crossing시 필요한 변수
    private int zeroCount;

    // 걸음 수
    private int step;

    // 측정된 거리
    private double distance;

    // 이동이 발생하였는지 여부
    private boolean IsMoved;


    // 초기화 메소드
    public void init()
    {
        // 변수 초기화
        preAcc = 0;
        newAcc = 0;
        HFilter_data = new double[1024];

        HFData_amount = 0;
        adjustValue = 5;


        LFilter_data = new double[1024];
        LFilterData_amount = 0;
        zeroCount = 0;

        step = 0;
        distance = 0;

        IsMoved = false;
    }

    /* *******************************************
     *
     *  PDR 기법 메소드
     *  : 가속도 벡터합으로 HPF -> LPF의 필터링을 거쳐
     *     Zero-Crossing을 통해 걸음 수를 측정한 후
     *     Weinberg Approach를 사용하여 보폭을 측정하여 총 거리를 계산한다.
     *
     * *******************************************/

    public void cal_PDR()
    {


        /*
         *  1. 가속도를 벡터합으로 계산
         *
         *    x, y, z 벡터 합성
         */

        cal_Vector();



        /*
         *  2. 고주파 필터링(High-pass Filtering (HPF))
         *    : HPF를 통해 중력 값을 제거한다.
         */

        cal_HPF();


        /*
         *  3. 저주파 필터링
         *
         *   -저주파 필터링(Low-pass Filtering (LPF))
         *    : LPF를 통해 노이즈 값을 제거한다.
         *
         *     HPF를 거친 값의 10개 평균을 계산한다.
         *
         */

        cal_LPF();
        if(HFData_amount == HFilter_data.length) HFData_amount = 0;



        /*
         *  4. Zero-crossing Method과 WeinBerg Approach
         *
         *   -Zero-crossing Method
         *    :  가속도가 0인곳을 찾아 한 주기만큼 잘라내기위한 메소드
         *       하나의 주기는 한 걸음으로 인식
         *
         *  -WeinBerg Approach
         *    : 걸음의 보폭을 측정하는 알고리즘.
         */

        cal_Step();





    }
    public void cal_Vector()
    {
        accVector = Math.sqrt(
                acc_X * acc_X +
                        acc_Y * acc_Y +
                        acc_Z * acc_Z);
    }

    public void cal_HPF()
    {
        // 적당한 상수
        final float alpha = (float)0.8;               // alpha = t / (t + Dt)

        // 현재 값에 중력 데이터를 빼서 가속도를 계산한다.
        preAcc = alpha * preAcc + (1 - alpha) * accVector;
        newAcc = accVector - preAcc;


        // HPF를 거친 데이터를 data배열에 담아놓는다.
        HFilter_data[HFData_amount++] = newAcc;
    }

    public void cal_LPF()
    {

        if(HFData_amount > 3)
        {
            int i, firstIndex = 0;
            double data = 0;
            // 5개 이상 데이터를 얻은 이후부터 실행
            int midIndex = HFData_amount - 4;


            if(midIndex < 5) firstIndex = midIndex;
            else if(midIndex >= 5 && midIndex < HFilter_data.length) firstIndex = midIndex - 5;


            // 데이터 10개의 평균을 낸다.
            for (i = firstIndex; i < midIndex + 4; i++)
            {
                data += HFilter_data[i];
            }
            data = data / 10.0;

            // 새롭게 필터된 데이터(LPF data)를 새로운 배열에 담아놓는다.
            LFilter_data[LFilterData_amount++] = data;
            adjustValue++;   // 정상적인 LPF값이면 조절변수 +1



            // 정지 상태일때를 걸러내기 위한 알고리즘
            // 정지상태일때의 데이터 평균값(-0.08 <= data <= 0.08)이 5번 연속이면, 그 값은 버린다.  정지 상태라고 판정
            // 이동상태의 데이터 평균값이 5번이상 연속적으로 입력된 경우에서 데이터가 0이면 zeroCount +1한다.
            if(data < 0.08 && data > -0.08 && adjustValue >= 5)
            {
                zeroCount++;
                adjustValue = 0;
            }
        }
    }


    public void cal_Step()
    {

        // 한 주기를 잘라내기 위하여 가속도 센서가 0에 근접할 때 count를 올리고
        // count 2가 되면 한 주기로 인식한다.
        if(zeroCount == 2){

            // 스마트폰이 조금만 민감하게 움직여도 가속도가 변화하여
            // 그 미세한 정도를 어느정도 필터하기 위한 과정.
            // 한 주기동안 얻어낸 데이터에서 가속도 절대값이 0.7이상 있을 경우만 승인.
            boolean access = false;
            for (int i = 0 ; i < LFilterData_amount ; i++)
            {
                if(LFilter_data[i] >= 0.7 || LFilter_data[i] <= -0.7)
                    access = true;
            }

            if(access){
                // 이제 한 주기를 잘라내 걸음 단위로 잘라내었다.
                // 걸음의 보폭을 계산한다.
                cal_StepSize();
                step++;   // 걸음 수 +1
            }

            // 한 걸음을 잘라내 계산 후 초기화 시키고 다시 반복한다.
            zeroCount = 0;
            LFilterData_amount = 0;
            LFilter_data = new double[1024];

        }

    }

    // Weinberg Approach
    public void cal_StepSize()
    {

        // 순차방문을 통해 비교하여 max와 min을 찾아낸다.
        double max = LFilter_data[0], min = LFilter_data[0];
        double temp;

        for (int i = 1; i<LFilter_data.length ; i++)
        {
            if(max < LFilter_data[i]) max = LFilter_data[i];
            if(min > LFilter_data[i]) min = LFilter_data[i];
        }

        // 보폭 상수 0.55
        final double k = 0.55;


        // Weinberg Approach
        temp = k*Math.pow(max-min, 1.0/4.0);

        // 정지 상태일때의 값 한번더 필터,  0.2이상만 값으로 인정
        if(temp >= 0.2)
        {
            distance += temp;
            IsMoved = true;
        }
    }


    // 가속도 값 받는 함수
    public void setAcc(float _acc_X, float _acc_Y, float _acc_Z)
    {
        acc_X = _acc_X;
        acc_Y = _acc_Y;
        acc_Z = _acc_Z;
    }


    public double getDistance() { return distance; }
    public void setDistance(double _distance) { distance = _distance; }
    public boolean getIsMoved() { return IsMoved; }
    public void setIsMoved(boolean _isMoved) { IsMoved = _isMoved; }
}
