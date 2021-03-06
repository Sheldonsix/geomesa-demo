package cn.edu.whut.springgeomesa.service.impl;

import cn.edu.whut.springgeomesa.config.IGeomesaDataConfig;
import cn.edu.whut.springgeomesa.config.impl.InfraredRayDataConfig;
import cn.edu.whut.springgeomesa.repository.IGeomesaRepository;
import cn.edu.whut.springgeomesa.service.IInfraredRayService;
import org.geotools.data.*;
import org.geotools.data.simple.SimpleFeatureCollection;
import org.geotools.data.simple.SimpleFeatureSource;
import org.geotools.data.simple.SimpleFeatureStore;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.filter.text.ecql.ECQL;
import org.geotools.geojson.feature.FeatureJSON;
import org.geotools.util.factory.Hints;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.filter.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @ClassName InfraredRayService
 * @Description TODO
 * @Author sheldon
 * @Date 2022/5/8 17:20
 * @Version 1.0
 **/
@Service
public class InfraredRayService implements IInfraredRayService {
    private static final Logger logger = (Logger) LoggerFactory.getLogger(InfraredRayService.class);
    private final IGeomesaRepository geomesaRepository;
    private final IGeomesaDataConfig iGeomesaDataConfig;
    private String dataTypeName;
//    private final InfraredRayDataConfig ird = new InfraredRayDataConfig();


    @Autowired
    public InfraredRayService(IGeomesaRepository geomesaRepository, @Qualifier("infraredRayDataConfig")IGeomesaDataConfig iGeomesaDataConfig) {
        this.geomesaRepository = geomesaRepository;
        this.iGeomesaDataConfig = iGeomesaDataConfig;
        this.dataTypeName = iGeomesaDataConfig.getTypeName();
    }
    
    /**
     * @author sheldon
     * @date 2022/5/8
     * @description ????????????
     * @param: params
     * @param: SID ?????????
     * @return java.lang.String
     **/
    @Override
    public String attributeQuery(Map<String, String> params, String waveLength, String SID) {
        try {
            // ???????????????
            DataStore dataStore = geomesaRepository.createDataStore(params);
            // ??????????????????
            Query query;
            if (SID != null) {
                query = new Query(dataTypeName, ECQL.toFilter("Wavelength > " + "'" + waveLength + "'" + " AND " + "Wavelength < " + "'" + (waveLength + 1) + "'" + " AND " +"SID = " + SID));
            } else {
                query = new Query(dataTypeName, ECQL.toFilter("Wavelength > "  + waveLength + " AND " + "Wavelength < " + (Integer.parseInt(waveLength) + 1)));
            }
            logger.info("???????????????" + ECQL.toCQL(query.getFilter()));
            if (query.getPropertyNames() != null) {
                logger.info("??????????????? " + Arrays.asList(query.getPropertyNames()));
            }
            // ?????? FeatureSource
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataTypeName);
            // ?????? featureCollection
            SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);
            FeatureJSON fjson = new FeatureJSON();
            StringWriter writer = new StringWriter();
            logger.info("???????????? GeoJson ????????? ");
            // ?????? GeoJson ??????
            fjson.writeFeatureCollection(featureCollection, writer);
            String json = writer.toString();
            return json;
        } catch (Exception e) {
            logger.error("???????????????????????????" + e);
            throw new RuntimeException("???????????????????????????" + e);
        }
    }
    
    /**
     * @author sheldon
     * @date 2022/5/8
     * @description ??????????????????????????????????????????
     * @param: params
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean insertInfraredRayData(Map<String, String> params) {
        try {
            // ???????????????
            DataStore dataStore = geomesaRepository.createDataStore(params);
            // ?????? SimpleFeatureType
            SimpleFeatureType sft = geomesaRepository.getSimpleFeatureType(iGeomesaDataConfig);
            // datastore ?????? SimpleFeatureType
            geomesaRepository.createSchema(dataStore, sft);
            List<SimpleFeature> features = geomesaRepository.getFeatures(iGeomesaDataConfig);
            if (features.size() > 0) {
                // ??????????????????
                geomesaRepository.writeFeatures(dataStore, sft, features);
            }
            System.out.println("????????? " + features.size() + "/" + "features");
            return true;
        } catch (Exception e) {
            logger.error("?????????????????????", e);
            throw new RuntimeException("?????????????????????", e);
        }
    }
/**
    private Map<String, String> defaultParams() {
        // ??????????????????
        // ??????????????? hbase.catalog: DMTest
        // HBase ????????? zookeeper ?????? hbase.zookeepers: localhost
        Map<String, String> params = new HashMap<>();
        params.put("hbase.catalog", "DMTest");
        params.put("hbase.zookeepers", "master,slave1,slave2");
        return params;
    }

    private void createDataStore(Map<String, String> params) {
        DataStore store = null;
        try {
            store = DataStoreFinder.getDataStore(params);
            if (store == null) {
                System.out.println("?????? datastore ??????????????????????????????");
                // ??????????????????????????????
                System.exit(-1);
            }
        } catch (Exception e) {
            System.out.println("?????? datastore ??????????????????????????????");
            e.printStackTrace();
            // ??????????????????????????????
            System.exit(-1);
        }
        this.dataStore = store;
    }

    public void writeData() throws IOException {
        System.out.println("??????????????????");
        writeInfraredRayData(this.ird);
    }

    private void writeInfraredRayData(InfraredRayDataConfig data) throws IOException {
        // ????????????
        FileWriter bww = new FileWriter("hbb_" + data.getTypeName() + ".csv");
        batchWriteInfraredRayData(data, bww);
        bww.flush();
        bww.close();
    }

    private void batchWriteInfraredRayData(InfraredRayDataConfig data, FileWriter w) throws IOException {
        // ?????? Schema
        System.out.println("?????? Schema");
        SimpleFeatureType sft = data.getSimpleFeatureType();
        createSchema(sft);
        // ????????????
        System.out.println("????????????");
        int count = 0;
        while (count < 500000) {
            List<SimpleFeature> features = data.getData(count, 1000);
            if (features.size() > 0) {
                batchWriteFeatures(sft, features, w);
            } else {
                break;
            }
            System.out.println("?????? " + features.size() + "/" + count + " features");
            count += features.size();
        }
    }

    private void createSchema(SimpleFeatureType simpleFeatureType) throws IOException {
        System.out.println("?????? Schema: " + DataUtilities.encodeType(simpleFeatureType));
        dataStore.createSchema(simpleFeatureType);
    }

    private void batchWriteFeatures(SimpleFeatureType simpleFeatureType, List<SimpleFeature> features, FileWriter w) throws IOException {
        if(features.size() > 0) {
            long startTime = System.currentTimeMillis();    //??????????????????
            try(FeatureWriter<SimpleFeatureType, SimpleFeature> writer =
                        dataStore.getFeatureWriterAppend(simpleFeatureType.getTypeName(), Transaction.AUTO_COMMIT)) {
                for (SimpleFeature feature :
                        features) {
                    // ???????????????????????????sf
                    SimpleFeature toWrite = writer.next();

                    // ???????????????
                    toWrite.setAttributes(feature.getAttributes());

                    // if you want to set the feature ID, you have to cast to an implementation class
                    // and add the USE_PROVIDED_FID hint to the user data
                    ((FeatureIdImpl) toWrite.getIdentifier()).setID(feature.getID());
                    toWrite.getUserData().put(Hints.USE_PROVIDED_FID, Boolean.TRUE);

                    // alternatively, you can use the PROVIDED_FID hint directly
                    // toWrite.getUserData().put(Hints.PROVIDED_FID, feature.getID());

                    // if no feature ID is set, a UUID will be generated for you

                    // make sure to copy the user data, if there is any
                    toWrite.getUserData().putAll(feature.getUserData());

                    // write the feature
                    writer.write();
                }
            }
            long endTime = System.currentTimeMillis();    // ??????????????????
            w.write((endTime - startTime) + "\n"); // ????????????????????????
        }
    }

    private void cleanup(String typeName) {
        if (this.dataStore != null) {
            try {
                System.out.println("Cleaning up test data");
                ((SimpleFeatureStore) this.dataStore.getFeatureSource(typeName)).removeFeatures(Filter.INCLUDE);
                this.dataStore.removeSchema(typeName);
            } catch (Exception e) {
                System.err.println("Exception cleaning up test data: " + e.toString());
            }
        }
    }
**/
    /**
     * @author sheldon
     * @date 2022/5/8
     * @description ???????????????????????????
     * @param: params
     * @return java.lang.Boolean
     **/
    @Override
    public Boolean deleteInfraredRayDatastore(Map<String, String> params) {
        try {
            DataStore dataStore = geomesaRepository.createDataStore(params);
            geomesaRepository.cleanup(dataStore, dataTypeName);
            return true;
        } catch (Exception e) {
            logger.error("?????????????????????", e);
            throw new RuntimeException("?????????????????????", e);
        }
    }

    /**
     * @author sheldon
     * @date 2022/5/17
     * @description ??????????????????
     * @param: params
     * @param: catalogName
     * @param: SID
     * @param: PID
     * @param: waveLength
     * @param: responseTime
     * @param: detectionRate
     * @return java.lang.String
     **/
    @Override
    public String spatiotemporalAttributeQuery(Map<String, String> params, String SID, String PID, String No, String waveLength) {
        try {
            // ???????????????
            DataStore dataStore = geomesaRepository.createDataStore(params);
            Query query = new Query(dataTypeName, ECQL.toFilter("SID = " + SID + " AND " + "PID = " + PID + " AND " + "Wavelength > " + waveLength +" AND " + "Wavelength < " + (Integer.parseInt(waveLength) + 1) + " AND " + "No = " + No),
                                    new String[]{"No", "WavelengthType"});
            logger.info("???????????????" + ECQL.toCQL(query.getFilter()));
            if (query.getPropertyNames() != null) {
                logger.info("??????????????? " + Arrays.asList(query.getPropertyNames()));
            }
            // ?????? FeatureSource
            SimpleFeatureSource featureSource = dataStore.getFeatureSource(dataTypeName);
            // ?????? featureCollection
            SimpleFeatureCollection featureCollection = featureSource.getFeatures(query);
            FeatureJSON featureJSON = new FeatureJSON();
            StringWriter writer = new StringWriter();
            logger.info("????????? GeoJson ????????? ");
            // ?????? GeoJson ????????????
            featureJSON.writeFeatureCollection(featureCollection, writer);
            String json = writer.toString();
            return json;
        } catch (Exception e) {
            logger.error("????????????????????????" + e);
            throw new RuntimeException("????????????????????????" + e);
        }
    }

}
