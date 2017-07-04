
#ifndef _EASR_INTERFACE_
#define _EASR_INTERFACE_

int GetEngineVersion();
int SetLogLevel(int level);
int SetTimeLogFile(const char* fn);
int SetSampleRateMode(int mode);
bool ExistFile(const char* file);
int GetDataDate(const char* fn);
int GetResLine(const char* fn);
int GetResVersion(const char* fn);
bool CheckDataMD5(const char* fn);
int DisableSSE4();

int ClearRes();
int Free();
int Initial();
int Initial(const char* dictFile, const char* userFile, const char* mmfFile, const char* hmmMapFile,
            short acoustic_model_mode=1,bool fastMode=false);
int BuildNet(int netTreeID,const char* synFile);
int WriteWdNonPron(const char* outFile);

int BuildSlot(char* buffer,long len);
int ReadSlot(const char* slotFile);
int ReadSlotLink(const char* slotLinkFile);
int ReadLM(const char* lmFn,const char* slotName,bool needBuildSlot);
int LoadRes(const char* lmFn,const char* lmSlotName,const char* slotFile,const char* slotLinkFile);

int SetVADEndCut(int id,bool vadEdForCut);
int InitialDecoder(int id,int vadID,int nBeam,double prunThres);
int ResetDecoder(int id);
int SetCurrNetTreeID(int id,int treeID,int mode=0);
int Fep(int id,short* data, int dataLen, bool bEd);
int Rec(int id, char** senArr, int expectNum);
char* GetImmeSentence(int id,int& frame);

char* DecodeAlways(int id,short* data, int dataLen, bool bEd);

int InitialMem();
int InitialVAD(int id,float max_speech_len,float max_speech_pause_len);
int ResetVAD(int id);
int VADDetect(int id,short* data, int dataLen, bool bEd);
int GetVadSt(int id);
int GetVadEd(int id);
long int get_dect_end_point_time(int id);

int KWSSetParam(int type,float value);
int KWSInitial(const char* kwdSynStr,const char* sFile,const char* resDir,int mode = 0);
int KWSReset(int* sceneArr,int n);
int KWSDecode(short* data, int dataLen, char** kwdArr, int expectNum, bool bEd);
int KWSDecode(short* data, int dataLen, char* outJsonStr, bool bEd);
int KWSFree();

int AudioSegReset();
int AudioSegFree();
int AudioSegInitial(const char* sFile,int mode=0);
int AudioSegDetect(short* data, int dataLen, bool bEd, int& spSt, int& spEd);
int AudioSegDetect(short* data, int dataLen, bool bEd, int& spSt, int& spEd, int *pauseEd);
float AudioSeggetSilConfidence(int contextLen);
int setParam(int function, int type, float value); //function: 1 = audioSeg
int AudioSegGetDelayFrameNum(); 

int WakeUpInitial(const char* wakeUpWd,const char* sFile,int mode=0);
int WakeUpReset();
int WakeUpDecode(short* data, int dataLen, char** senArr, int expectNum, bool bEd);
int WakeUpDecode(short* data, int dataLen, char** senArr, int expectNum, int &wakeword_frame_len, bool bEd);
int WakeUpDecode(short* data, int dataLen, char** senArr, int expectNum, int &wakeword_frame_len,
        bool &is_confidence, int &voice_offset, bool bEd);
int WakeUpFree();
int WakeUpSetEnvironment(int status);//0 is quiet,else is noise

int AECInit();
int AECReset();
short* AECProcess(short* data_in,short* wav_ref,long len_in);
int AECExit();

int GetPyED(const char* str1,const char* str2);
int GetStateED(const char* str1,const char* str2);

extern char* ChnNameSegPreProcess(const char* aNameArr,const char* aResFileName);
extern char* EngNamePronPreProcess(const char* aNameArr);

int SetLicenseFlag(bool lic_flag);
bool GetLicenseFlag();

#ifdef _ANDROID_
#include <jni.h>
int GetLicense(JNIEnv *env, jclass obj,jobject context,jstring appCode,jstring cuid,jstring stat,jstring license_file);
int VerifyLicense(JNIEnv *env,jobject context,jstring appCode,jstring cuid,jbyteArray license,jint len,jbyteArray appIDSeq,jstring logDir);
#else
#ifdef _IOS_
int GetLicense(const char* appCode,const char* stat,const char* license_file);
int VerifyLicense(const char* appCode,const char* license,int len,unsigned int& appID,const char* logDir);
#else //x86
int GetLicense(const char* appName,const char* appCode,const char* stat,const char* license_file);
int VerifyLicense(const char* appName,const char* appCode,const char* license,int len,unsigned int& appID,const char* logDir);
#endif
#endif
int OpenPVUpload();
int ClosePVUpload();
int GetTestAuthorize();
int GetLongAuthorize();
bool GetAuthorize();


#endif
